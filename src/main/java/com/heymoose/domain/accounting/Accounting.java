package com.heymoose.domain.accounting;

import com.heymoose.domain.Withdraw;

import java.math.BigDecimal;
import java.util.List;

public interface Accounting {
  void transferMoney(Account src, Account dst, BigDecimal amount,
                     AccountingEvent event, Long sourceId);

  void applyEntry(AccountingEntry entry);

  void transferMoney(Account src, Account dst, BigDecimal amount,
                     AccountingEvent event, Long sourceId, String descr);

  void transferMoney(Account src, AccountingEntry dstEntry, BigDecimal amount,
                     AccountingEvent event, Long sourceId, String desc);

  void cancel(AccountingTransaction transaction);

  AccountingEntry getLastEntry(Account account);

  Withdraw withdraw(Account account, BigDecimal amount);

  List<Withdraw> withdraws(Account account);

  Withdraw withdrawOfAccount(Account account, long withdrawId);

  void deleteWithdraw(Withdraw withdraw, String comment);

  Account destination(AccountingTransaction transaction);
}
