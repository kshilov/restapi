package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.inject.name.Named;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.AdminAccountAccessor;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
import com.heymoose.infrastructure.util.db.TemplateQuery;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class OfferActionsHiber implements OfferActions {

  private static final Logger log =
      LoggerFactory.getLogger(OfferActionsHiber.class);

  private final Accounting accounting;
  private final Repo repo;
  private final AdminAccountAccessor adminAccountAccessor;
  private final Debts debts;
  private final BigDecimal mlmRate;

  @Inject
  public OfferActionsHiber(Accounting accounting, Repo repo, Debts debts,
                           AdminAccountAccessor adminAccountAccessor,
                           @Named("mlm-ratio") double mlmRatio) {
    this.accounting = accounting;
    this.repo = repo;
    this.adminAccountAccessor = adminAccountAccessor;
    this.debts = debts;
    this.mlmRate = new BigDecimal(mlmRatio);
  }

  @Override
  public Integer approveExpired(Offer offer) {
    DateTime start = DateTime.now();
    String hql = "from OfferAction a where " +
        (offer != null ? "a.offer.id in (:offerIds) and " : "") +
        "a.state = :state " +
        "and cast(now() as date) - cast(a.creationTime as date) > a.offer.holdDays";

    Query query = repo.session().createQuery(hql);
    if (offer != null) query.setParameterList("offerIds", offer.subofferIds());
    query.setParameter("state", OfferActionState.NOT_APPROVED);

    @SuppressWarnings("unchecked")
    List<OfferAction> expiredActions = query.list();

    for (OfferAction action : expiredActions)
      approve(action);

    log.info("Total approve time: {}",
        Period.fieldDifference(
            DateTime.now().toLocalTime(),
            start.toLocalTime()));
    return expiredActions.size();
  }

  @Override
  public Integer cancelByTransactions(Offer offer, Set<String> transactionIds) {
    return verify(offer, transactionIds, OfferActionState.CANCELED);
  }

  @Override
  public int verify(Offer offer, Collection<String> transactionIdList,
                     OfferActionState state) {
    if (transactionIdList.isEmpty()) return 0;
    @SuppressWarnings("unchecked")
    List<OfferAction> actionsToVerify = repo.session()
        .createCriteria(OfferAction.class)
        .add(Restrictions.in("offer.id", offer.subofferIds()))
        .add(Restrictions.eq("state", OfferActionState.NOT_APPROVED))
        .add(Restrictions.in("transactionId", transactionIdList))
        .list();
    for (OfferAction action : actionsToVerify) {
      switch (state) {
        case APPROVED:
          approve(action);
          break;
        case CANCELED:
          cancel(action);
          break;
        default:
          throw new IllegalArgumentException("Wrong state " + state);
      }
    }
    return actionsToVerify.size();
  }

  @Override
  public void approve(OfferAction action) {
    log.info("Approving action {}.", action.id());
    checkArgument(action.state() == OfferActionState.NOT_APPROVED);
    User affiliate = action.affiliate();

    // entry to affiliate not confirmed account
    AccountingEntry affEntry = (AccountingEntry) repo.session()
        .createCriteria(AccountingEntry.class)
        .add(Restrictions.eq("account", affiliate.affiliateAccountNotConfirmed()))
        .add(Restrictions.gt("amount", BigDecimal.ZERO))
        .add(Restrictions.eq("event", AccountingEvent.ACTION_CREATED))
        .add(Restrictions.eq("sourceId", action.id()))
        .uniqueResult();

    // entry to admin not confirmed account
    AccountingEntry adminEntry = (AccountingEntry) repo.session()
        .createCriteria(AccountingEntry.class)
        .add(Restrictions.eq("account", adminAccountAccessor.getAdminAccountNotConfirmed()))
        .add(Restrictions.gt("amount", BigDecimal.ZERO))
        .add(Restrictions.eq("event", AccountingEvent.ACTION_CREATED))
        .add(Restrictions.eq("sourceId", action.id()))
        .uniqueResult();

    // approve affiliate money
    accounting.newTransfer()
        .from(affiliate.affiliateAccountNotConfirmed())
        .to(affiliate.affiliateAccount())
        .amount(affEntry.amount())
        .event(AccountingEvent.ACTION_APPROVED)
        .sourceId(action.id())
        .execute();
    action.stat().approveMoney(affEntry.amount());
    debts.oweAffiliateRevenue(action, affEntry.amount());

    // approve admin money
    BigDecimal mlmValue = BigDecimal.ZERO;
    if (affiliate.referrerId() != null) {
      mlmValue = mlmRate.multiply(affEntry.amount());
    }
    // admin entry can be null, e.g. for referral offer actions
    if (adminEntry != null) {
      BigDecimal adminValue = adminEntry.amount().subtract(mlmValue);
      accounting.newTransfer()
          .from(adminAccountAccessor.getAdminAccountNotConfirmed())
          .to(adminAccountAccessor.getAdminAccount())
          .amount(adminValue)
          .event(AccountingEvent.ACTION_APPROVED)
          .sourceId(action.id())
          .execute();
      action.stat().approveFee(adminValue);
      debts.oweFee(action, adminValue);
    }

    // approve mlm
    if (mlmValue.signum() == 1) {
      log.info("Approving mlm: {} to user: {}",
          mlmValue, affiliate.referrerId());
      accounting.newTransfer()
          .from(adminAccountAccessor.getAdminAccountNotConfirmed())
          .to(repo.get(User.class, affiliate.referrerId()).affiliateAccount())
          .amount(mlmValue)
          .event(AccountingEvent.MLM)
          .sourceId(action.id())
          .execute();
      debts.oweMlm(action, mlmValue);
    }
    action.stat().incConfirmedActions();
    action.approve();
  }

  @Override
  public void cancel(OfferAction action) {
    checkArgument(action.state() == OfferActionState.NOT_APPROVED);
    List<AccountingEntry> entries = repo.allByHQL(
        AccountingEntry.class,
        "from AccountingEntry where amount < 0 and event = ? and sourceId = ?",
        AccountingEvent.ACTION_CREATED,
        action.id()
    );
    for (AccountingEntry entry : entries) {
      Account dst = accounting.destination(entry.transaction());
      if (dst.equals(action.affiliate().affiliateAccountNotConfirmed())) {
        accounting.transferMoney(
            action.affiliate().affiliateAccountNotConfirmed(),
            action.offer().account(),
            entry.amount().negate(),
            AccountingEvent.ACTION_CANCELED,
            action.id()
        );
        action.stat().cancelMoney(entry.amount().negate());
      } else if (dst.equals(adminAccountAccessor.getAdminAccountNotConfirmed())) {
        accounting.transferMoney(
            adminAccountAccessor.getAdminAccountNotConfirmed(),
            action.offer().account(),
            entry.amount().negate(),
            AccountingEvent.ACTION_CANCELED,
            action.id()
        );
        action.stat().cancelFee(entry.amount().negate());
      }
    }
    action.stat().incCanceledActions();
    action.cancel();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void cancelByIdList(Offer offer, Collection<Long> idCollection) {
    for (OfferAction action : listActions(offer, idCollection)) {
      cancel(action);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void approveByIdList(Offer offer, List<Long> idList) {
    for (OfferAction action : listActions(offer, idList)) {
      approve(action);
    }
  }

  @Override
  @Transactional
  public void fix() {
    List<OfferAction> approvedActions = repo.allByHQL(
        OfferAction.class, "from OfferAction where state = ?", OfferActionState.APPROVED);
    for (OfferAction action : approvedActions) {
      List<AccountingEntry> actionEntries = repo.allByHQL(
          AccountingEntry.class,
          "from AccountingEntry where amount < 0 and event = ? and sourceId = ?",
          AccountingEvent.ACTION_APPROVED, action.id());
      if (actionEntries.size() <= 2)
        continue;
      List<AccountingEntry> affEntries = newArrayList();
      List<AccountingEntry> admEntries = newArrayList();
      for (AccountingEntry entry : actionEntries) {
        Account dst = accounting.destination(entry.transaction());
        if (dst.equals(action.affiliate().affiliateAccount()))
          affEntries.add(entry);
        if (dst.equals(adminAccountAccessor.getAdminAccount()))
          admEntries.add(entry);
      }
      if (affEntries.isEmpty() || admEntries.isEmpty()) {
        log.info("WARNING: strange entries");
        log.info("affEntries.size(): " + affEntries.size());
        log.info("admEntries.size(): " + admEntries.size());
        for (AccountingEntry e : actionEntries)
          log.info(e.toString());
        continue;
      }
      Comparator<AccountingEntry> byDateDesc = new Comparator<AccountingEntry>() {
        @Override
        public int compare(AccountingEntry o1, AccountingEntry o2) {
          return o2.creationTime().compareTo(o1.creationTime());
        }
      };
      Collections.sort(affEntries, byDateDesc);
      Collections.sort(admEntries, byDateDesc);
      AccountingEntry affEntry = affEntries.get(0);
      AccountingEntry admEntry = admEntries.get(0);
      DateTime boomDate = DateTime.parse("2012-06-25");
      if (affEntry.creationTime().isBefore(boomDate) || admEntry.creationTime().isBefore(boomDate))
        continue;
      log.info("Cancelling tx: " + affEntry.transaction());
      log.info("Cancelling tx: " + admEntry.transaction());
      accounting.cancel(affEntry.transaction());
      accounting.cancel(admEntry.transaction());
      action.stat().addToNotConfirmedRevenue(affEntry.amount().negate());
      action.stat().subtractFromConfirmedRevenue(affEntry.amount().negate());
    }
  }

  @Override
  public Pair<QueryResult, Long> list(Long offerId, OfferActionState state,
                                      DateKind dateKind,
                                      DataFilter<Ordering> filter) {
    TemplateQuery query =
        SqlLoader.templateQuery("offer_actions", repo.session())
            .addQueryParam("offer_id", offerId)
            .addQueryParam("from", filter.from())
            .addQueryParam("to", filter.to())
            .addTemplateParam("ordering", filter.ordering())
            .addTemplateParam("direction", filter.direction())
            .addTemplateParamIfNotNull(state, "filterByState", true);
    if (state != null) {
      query.addQueryParam("state", state.ordinal());
    }
    switch (dateKind) {
      case CREATION:
        query.addTemplateParam("filterByCreationTime", true);
        break;
      case CHANGE:
        query.addTemplateParam("filterByCreationTime", true);
        break;
    }
    return query.executeAndCount(filter.offset(), filter.limit());
  }

  @Override
  public List<OfferAction> listProductActions(Token token,
                                              String transactionId,
                                              Product product) {
    return repo.allByHQL(OfferAction.class,
        "from OfferAction where token = ? and transactionId = ? and product = ?",
        token, transactionId, product);
  }

  @Override
  public List<OfferAction> list(Token token, String transactionId) {
    return repo.allByHQL(OfferAction.class,
        "from OfferAction where token = ? and transactionId = ?",
        token, transactionId);
  }

  @SuppressWarnings("unchecked")
  private Iterable<OfferAction> listActions(Offer offer,
                                            Collection<Long> idCollection) {
    Preconditions.checkNotNull(offer);
    return (List<OfferAction>) repo.session().createQuery(
        "from OfferAction where offer.id in (:sub_list) and id in (:id_list)")
        .setParameterList("sub_list", offer.subofferIds())
        .setParameterList("id_list", idCollection)
        .list();
  }

}
