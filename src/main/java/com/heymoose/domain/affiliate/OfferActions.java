package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.AdminAccountAccessor;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OfferActions {

  private final Accounting accounting;
  private final Repo repo;
  private final AdminAccountAccessor adminAccountAccessor;

  @Inject
  public OfferActions(Accounting accounting, Repo repo, AdminAccountAccessor adminAccountAccessor) {
    this.accounting = accounting;
    this.repo = repo;
    this.adminAccountAccessor = adminAccountAccessor;
  }

  public Integer approveExpired(Offer offer) {
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
    return expiredActions.size();
  }

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

  public void approve(OfferAction action) {
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
  }

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
      }
    }
    action.cancel();
  }

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
        logger.info("WARNING: strange entries");
        logger.info("affEntries.size(): " + affEntries.size());
        logger.info("admEntries.size(): " + admEntries.size());
        for (AccountingEntry e : actionEntries)
          logger.info(e.toString());
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
      logger.info("Cancelling tx: " + affEntry.transaction());
      logger.info("Cancelling tx: " + admEntry.transaction());
      accounting.cancel(affEntry.transaction());
      accounting.cancel(admEntry.transaction());
      action.stat().addToNotConfirmedRevenue(affEntry.amount().negate());
      action.stat().subtractFromConfirmedRevenue(affEntry.amount().negate());
    }
  }

  private final static Logger logger = LoggerFactory.getLogger(OfferActions.class);
}
