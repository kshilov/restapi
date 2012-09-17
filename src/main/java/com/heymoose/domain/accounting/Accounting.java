package com.heymoose.domain.accounting;

import com.heymoose.domain.offer.Offer;

import java.math.BigDecimal;

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

  Account destination(AccountingTransaction transaction);

  void addOfferFunds(Offer offer, BigDecimal amount);

}
