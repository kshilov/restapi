package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.user.AdminAccountAccessor;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.ImmutableMapTransformer;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigInteger;
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

  @Inject
  public OfferActionsHiber(Accounting accounting, Repo repo,
                           AdminAccountAccessor adminAccountAccessor) {
    this.accounting = accounting;
    this.repo = repo;
    this.adminAccountAccessor = adminAccountAccessor;
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
    if (transactionIds.isEmpty())
      return 0;
    String hql = "from OfferAction a where a.offer.id in (:offerIds) and a.state = :state " +
        "and a.transactionId in (:transactionIds)";

    @SuppressWarnings("unchecked")
    List<OfferAction> actionsToCancel = repo.session().createQuery(hql)
        .setParameterList("offerIds", offer.subofferIds())
        .setParameter("state", OfferActionState.NOT_APPROVED)
        .setParameterList("transactionIds", transactionIds)
        .list();
    for (OfferAction action : actionsToCancel)
      cancel(action);
    return actionsToCancel.size();
  }

  @Override
  public void approve(OfferAction action) {
    log.info("Approving action {}.", action.id());
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
            action.affiliate().affiliateAccount(),
            entry.amount().negate(),
            AccountingEvent.ACTION_APPROVED,
            action.id()
        );
        action.stat().approveMoney(entry.amount().negate());
      } else if (dst.equals(adminAccountAccessor.getAdminAccountNotConfirmed())) {
        accounting.transferMoney(
            adminAccountAccessor.getAdminAccountNotConfirmed(),
            adminAccountAccessor.getAdminAccount(),
            entry.amount().negate(),
            AccountingEvent.ACTION_APPROVED,
            action.id()
        );
        action.stat().approveFee(entry.amount().negate());
      }
    }
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
    action.cancel();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void cancelByIdList(Offer offer, Collection<Long> idCollection) {
    Preconditions.checkNotNull(offer);
    List<OfferAction> actionList = (List<OfferAction>) repo.session().createQuery(
        "from OfferAction where offer.id in (:sub_list) and id in (:id_list)")
        .setParameter("sub_list", offer.subofferIds())
        .setParameterList("id_list", idCollection)
        .list();
    for (OfferAction action : actionList) {
      cancel(action);
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
  @SuppressWarnings("unchecked")
  public Pair<QueryResult, Long> list(Long offerId, OfferActionState state,
                                      ListFilter filter,
                                      Ordering ordering, OrderingDirection direction) {
    ImmutableMap.Builder<String, Object> templateParams =
        ImmutableMap.<String, Object>builder()
        .put("ordering", ordering)
        .put("direction", direction);
    if (state != null) {
      templateParams.put("filterByState", true);
    }
    String sql = SqlLoader.getTemplate("offer_actions", templateParams.build());
    Query query = repo.session().createSQLQuery(sql)
        .setParameter("offset", filter.offset())
        .setParameter("limit", filter.limit())
        .setParameter("from", filter.from().toDate())
        .setParameter("to", filter.to().toDate())
        .setParameter("offer_id", offerId)
        .setResultTransformer(ImmutableMapTransformer.INSTANCE);

    String countSql = SqlLoader.countSql(sql);
    Query countQuery = repo.session().createSQLQuery(countSql)
        .setParameter("offer_id", offerId)
        .setParameter("from", filter.from().toDate())
        .setParameter("to", filter.to().toDate());

    if (state != null) {
      query.setParameter("state", state.ordinal());
      countQuery.setParameter("state", state.ordinal());
    }

    QueryResult listResult = new QueryResult(query.list());
    BigInteger count = (BigInteger) countQuery.uniqueResult();
    return Pair.of(listResult, count.longValue());
  }

}
