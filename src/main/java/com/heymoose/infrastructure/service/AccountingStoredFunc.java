package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.accounting.AccountingTransaction;
import com.heymoose.domain.accounting.Withdraw;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
  public Withdraw withdraw(Account account, BigDecimal amount) {
    return accountingHiber.withdraw(account, amount);
  }
  
  @Override
  public void approveWithdraw(Withdraw withdraw) {
    accountingHiber.approveWithdraw(withdraw);
  };

  @Override
  public List<Withdraw> withdraws(Account account) {
    return accountingHiber.withdraws(account);
  }

  @Override
  public Withdraw withdrawOfAccount(Account account, long withdrawId) {
    return accountingHiber.withdrawOfAccount(account, withdrawId);
  }

  @Override
  public void deleteWithdraw(Withdraw withdraw, String comment) {
    accountingHiber.deleteWithdraw(withdraw, comment);
  }

  @Override
  public Account destination(AccountingTransaction transaction) {
    return accountingHiber.destination(transaction);
  }

  @Override
  public Pair<QueryResult, Long> debtGroupedByAffiliate(
      Long offerId, DataFilter<DebtOrdering> filter) {
    return accountingHiber.debtGroupedByAffiliate(offerId, filter);
  }


  @Override
  public Map<String, Object> sumDebtForAffiliate(Long affId, DateTime from,
                                                 DateTime to) {
    return accountingHiber.sumDebtForAffiliate(affId, from, to);
  }

  @Override
  public Pair<QueryResult, Long> debtGroupedByOffer(
      Long affId, DataFilter<DebtOrdering> filter) {
    return accountingHiber.debtGroupedByOffer(affId, filter);
  }

  @Override
  public Map<String, Object> sumDebtForOffer(Long offerId, DateTime from,
                                             DateTime to) {
    return accountingHiber.sumDebtForOffer(offerId, from, to);
  }
}
