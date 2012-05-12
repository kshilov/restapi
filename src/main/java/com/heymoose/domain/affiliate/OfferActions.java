package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.AdminAccountAccessor;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.Query;

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
  
  @Transactional
  public Integer approveExpired(long offerId) {
    String hql = "from OfferAction a where a.stat.offer.id = :offerId and a.state = :state " +
    		"and cast(now() as date) - cast(a.creationTime as date) > a.offer.holdDays";
    List<OfferAction> expiredActions = repo.session().createQuery(hql)
        .setParameter("offerId", offerId)
        .setParameter("state", OfferActionState.NOT_APPROVED)
        .list();
    for (OfferAction action : expiredActions)
      approve(action);
    return expiredActions.size();
  }
  
  @Transactional
  public Integer cancelByTransactions(long offerId, Set<String> transactionIds) {
    if (transactionIds.isEmpty())
      return 0;
    String hql = "from OfferAction a where a.stat.offer.id = :offerId and a.state = :state " +
    		"and a.transactionId in (:transactionIds)";
    List<OfferAction> actionsToCancel = repo.session().createQuery(hql)
        .setParameter("offerId", offerId)
        .setParameter("state", OfferActionState.NOT_APPROVED)
        .setParameterList("transactionIds", transactionIds)
        .list();
    for (OfferAction action : actionsToCancel)
      cancel(action);
    return actionsToCancel.size();
  }

  @Transactional
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

  @Transactional
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
      if (dst.equals(action.stat().affiliate().affiliateAccountNotConfirmed())) {
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
}
