package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Singleton
public final class AccountingStoredFunc implements Accounting {

  private static final Logger log =
      LoggerFactory.getLogger(AccountingStoredFunc.class);

  private final Accounting accountingHiber;
  private final Repo repo;

  @Inject
  public AccountingStoredFunc(AccountingHiber accountingHiber, Repo repo) {
    this.accountingHiber = accountingHiber;
    this.repo = repo;
  }

  @Override
  public void transferMoney(Account src, Account dst, BigDecimal amount,
                            AccountingEvent event, Long sourceId) {
    this.transferMoney(src, dst, amount, event, sourceId, null);
  }

  @Override
  public void applyEntry(AccountingEntry entry) {
    this.accountingHiber.applyEntry(entry);
  }

  @Override
  public void transferMoney(Account src, Account dst, BigDecimal amount,
                            AccountingEvent event, Long sourceId,
                            String descr) {
    Object transactionId = repo.session().createSQLQuery(
          "select transfer_money("
            + ":amount, "
            + "cast(:descr as varchar(255)), "
            + ":event, "
            + ":source_id, "
            + ":account_from_id, "
            + ":account_to_id"
          + ");")
        .setParameter("amount", amount)
        .setParameter("descr", descr)
        .setParameter("event", event.ordinal())
        .setParameter("source_id", sourceId)
        .setParameter("account_from_id", src.id())
        .setParameter("account_to_id", dst.id())
        .uniqueResult();
    log.info("Money transfer success. Transaction id: {}",
        transactionId);
  }

  @Override
  public void transferMoney(Account src, AccountingEntry dstEntry,
                            BigDecimal amount, AccountingEvent event,
                            Long sourceId, String desc) {
    accountingHiber.transferMoney(src, dstEntry, amount, event, sourceId, desc);
  }

  @Override
  public void cancel(AccountingTransaction transaction) {
    accountingHiber.cancel(transaction);
  }

  @Override
  public AccountingEntry getLastEntry(Account account) {
    return accountingHiber.getLastEntry(account);
  }

  @Override
  public Account destination(AccountingTransaction transaction) {
    return accountingHiber.destination(transaction);
  }

  @Override
  public void addOfferFunds(Offer offer, BigDecimal amount) {
    accountingHiber.addOfferFunds(offer, amount);
  }
}
