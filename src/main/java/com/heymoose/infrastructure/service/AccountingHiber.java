package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.user.AdminAccountAccessor;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class AccountingHiber implements Accounting {

  private static final Logger log =
      LoggerFactory.getLogger(AccountingHiber.class);

  private final Repo repo;
  private final AdminAccountAccessor adminAccounts;

  @Inject
  public AccountingHiber(Repo repo, AdminAccountAccessor adminAccounts) {
    this.repo = repo;
    this.adminAccounts = adminAccounts;
  }

  @Override
  public Transfer newTransfer() {
    return new Transfer(this);
  }

  @Override
  public void transferMoney(Account src, Account dst, BigDecimal amount,
                            AccountingEvent event, Long sourceId) {
    transferMoney(src, dst, amount, event, sourceId, null);
  }

  @Override
  public void applyEntry(AccountingEntry entry) {
    repo.put(entry);
    Session session = repo.session();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", entry.amount())
        .setParameter("id", entry.account().id())
        .executeUpdate();
    session.refresh(entry.account());
  }

  @Override
  public void transferMoney(Account src, Account dst, BigDecimal amount,
                            AccountingEvent event, Long sourceId, String descr) {
    checkArgument(amount.signum() == 1);
    log.info("Entering transfer money src: {} dst: {}, " +
        "amount: {}, event: {}, sourceId: {}, descr: {}",
        new Object[] { src.id(), dst.id(), amount, event, sourceId, descr });
    AccountingEntry srcEntry = new AccountingEntry(src, amount.negate(), event, sourceId, descr);
    AccountingEntry dstEntry = new AccountingEntry(dst, amount, event, sourceId, descr);
    createTransaction(srcEntry, dstEntry);
    Session session = repo.session();
    session.createQuery("update Account set balance = balance - :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", src.id())
        .executeUpdate();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", dstEntry.account().id())
        .executeUpdate();
    session.refresh(dst);
    session.refresh(src);
  }

  @Override
  public void transferMoney(Account src, AccountingEntry dstEntry,
                            BigDecimal amount, AccountingEvent event,
                            Long sourceId, String desc) {
    checkArgument(amount.signum() == 1);
    Session session = repo.session();
    session.createQuery("update AccountingEntry set amount = amount + :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", dstEntry)
        .executeUpdate();
    session.refresh(dstEntry);
    AccountingEntry srcEntry = new AccountingEntry(src, amount.negate(), event, sourceId, desc);
    srcEntry.setTransaction(dstEntry.transaction());
    repo.put(srcEntry);
    session.createQuery("update Account set balance = balance - :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", src.id())
        .executeUpdate();
    session.createQuery("update Account set balance = balance + :amount where id = :id")
        .setParameter("amount", amount)
        .setParameter("id", dstEntry.account().id())
        .executeUpdate();
    session.refresh(src);
  }

  @Override
  public void cancel(AccountingTransaction transaction) {
    List<AccountingEntry> entries = repo.allByHQL(
        AccountingEntry.class,
        "from AccountingEntry where transaction = ?",
        transaction
    );
    AccountingTransaction reverseTx = new AccountingTransaction();
    for (AccountingEntry entry : entries) {
      AccountingEntry reverseEntry = new AccountingEntry(entry.account(), entry.amount().negate(), AccountingEvent.CANCELLED, entry.id(), null);
      reverseEntry.setTransaction(reverseTx);
      applyEntry(reverseEntry);
    }
  }

  private void createTransaction(AccountingEntry srcEntry, AccountingEntry dstEntry) {
    AccountingTransaction transaction = new AccountingTransaction();
    srcEntry.setTransaction(transaction);
    dstEntry.setTransaction(transaction);
    repo.put(srcEntry);
    repo.put(dstEntry);
  }

  @Override
  public AccountingEntry getLastEntry(Account account) {
    DetachedCriteria criteria = DetachedCriteria.forClass(AccountingEntry.class);
    criteria.add(Restrictions.eq("account", account));
    criteria.addOrder(Order.desc("id"));
    return (AccountingEntry) repo.getExecutableCriteria(criteria).setMaxResults(1).uniqueResult();
  }

  @Override
  public Account destination(AccountingTransaction transaction) {
    return repo.byHQL(
        Account.class,
        "select e.account from AccountingEntry e where e.transaction = ? and e.amount > 0",
        transaction
    );
  }

  @Override
  public void addOfferFunds(Offer offer, BigDecimal amount, Long sourceId) {
    Account advertiserAcc = offer.advertiser().advertiserAccount();
    Preconditions.checkArgument(amount.signum() > 0,
        "Amount should be positive.");
    Preconditions.checkArgument(advertiserAcc.balance().compareTo(amount) >= 0,
        "Can't transfer more, than advertiser has on his account.");
    this.transferMoney(advertiserAcc, offer.account(), amount,
        AccountingEvent.OFFER_ACCOUNT_ADD, sourceId);
  }

  @Override
  public void addOfferFunds(Offer offer, BigDecimal amount) {
    Account advertiserAcc = offer.advertiser().advertiserAccount();
    Preconditions.checkArgument(amount.signum() > 0,
        "Amount should be positive.");
    Preconditions.checkArgument(advertiserAcc.balance().compareTo(amount) >= 0,
        "Can't transfer more, than advertiser has on his account.");
    this.transferMoney(advertiserAcc, offer.account(), amount,
        AccountingEvent.OFFER_ACCOUNT_ADD, null);
  }

  @Override
  public void notConfirmedActionPayments(OfferAction action,
                                         BigDecimal affiliatePart,
                                         BigDecimal heymoosePart) {

    this.newTransfer()
        .from(action.offer().account())
        .to(action.affiliate().affiliateAccountNotConfirmed())
        .amount(affiliatePart)
        .event(AccountingEvent.ACTION_CREATED)
        .sourceId(action.id()).execute();
    if (heymoosePart.signum() == 0) return;
    this.newTransfer()
        .from(action.offer().account())
        .to(adminAccounts.getAdminAccountNotConfirmed())
        .amount(heymoosePart)
        .event(AccountingEvent.ACTION_CREATED)
        .sourceId(action.id()).execute();
  }
}
