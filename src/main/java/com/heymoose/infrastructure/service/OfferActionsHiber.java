package com.heymoose.infrastructure.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.user.AdminAccountAccessor;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.OrderingDirection;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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
    DateTime start = DateTime.now();
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
      }
    }
    action.approve();
    log.info("Approve time: {}",
        Period.fieldDifference(
            DateTime.now().toLocalTime(),
            start.toLocalTime()));
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
  public List<OfferAction> list(Long offerId, OfferActionState state,
                                ListFilter filter,
                                Ordering ordering,
                                OrderingDirection direction) {
    List<SubOffer> subOfferList = repo.allByHQL(
        SubOffer.class,
        "from SubOffer where parentId = ?", offerId);
    List<Long> subOfferIdList = Lists.newArrayList();
    for (SubOffer sub : subOfferList) {
      subOfferIdList.add(sub.id());
    }
    Criteria criteria = repo.session().createCriteria(OfferAction.class)
        .createAlias("stat", "stat")
        .createAlias("affiliate", "affiliate")
        .setFirstResult(filter.offset())
        .setMaxResults(filter.limit());

    if (subOfferIdList.size() > 0) {
      criteria.add(Restrictions.or(
          Restrictions.eq("offer.id", offerId),
          Restrictions.in("offer.id", subOfferIdList)));
    } else {
      criteria.add(Restrictions.eq("offer.id", offerId));
    }

    if (state != null)
      criteria.add(Restrictions.eq("state", state));

    List<String> orderingFieldNameList = ImmutableList.of("stat.creationTime");
    switch (ordering) {
      case TRANSACTION_ID:
        orderingFieldNameList = ImmutableList.of("transactionId");
        break;
      case CREATION_TIME:
        orderingFieldNameList = ImmutableList.of("creationTime");
        break;
      case AFFILIATE_ID:
        orderingFieldNameList = ImmutableList.of("affiliate.id");
        break;
      case AFFILIATE_EMAIL:
        orderingFieldNameList = ImmutableList.of("affiliate.email");
        break;
      case STATE:
        orderingFieldNameList = ImmutableList.of("state");
        break;
      case AMOUNT:
        orderingFieldNameList = ImmutableList.of(
            "stat.notConfirmedRevenue",
            "stat.canceledRevenue",
            "stat.confirmedRevenue");
        break;
    }
    switch (direction) {
      case ASC:
        for (String fieldName : orderingFieldNameList) {
          criteria.addOrder(Order.asc(fieldName));
        }
        break;
      case DESC:
        for (String fieldName : orderingFieldNameList) {
          criteria.addOrder(Order.desc(fieldName));
        }
        break;
    }
    return (List<OfferAction>) criteria.list();
  }


  @Override
  public Long count(Long offerId, OfferActionState state) {
    Criteria criteria =  repo.session().createCriteria(OfferAction.class)
        .add(Restrictions.eq("offer.id", offerId))
        .setProjection(Projections.count("id"));
    if (state != null)
      criteria.add(Restrictions.eq("state", state));
    return (Long) criteria.uniqueResult();

  }
}
