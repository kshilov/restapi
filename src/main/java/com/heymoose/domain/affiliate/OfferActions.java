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
  public void approveAllExpired(long offerId, Set<String> excluding) {
    List<OfferAction> expiredActions = repo.session().createQuery(
        "from OfferAction a where a.stat.offer.id = :offerId and a.state = :state and a.transactionId not in (:excluding)")
        .setParameter("offerId", offerId)
        .setParameter("state", OfferActionState.NOT_APPROVED)
        .setParameterList("excluding", excluding)
        .list();
    for (OfferAction action : expiredActions)
      approve(action);
    List<OfferAction> excludingActions = repo.session().createQuery("from OfferAction a where a.transactionId in (:excluding)")
        .setParameterList("excluding", excluding)
        .list();
    for (OfferAction action : excludingActions)
      cancel(action);
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
