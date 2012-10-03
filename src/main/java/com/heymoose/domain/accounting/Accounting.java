package com.heymoose.domain.accounting;

import com.heymoose.domain.offer.Offer;

import java.math.BigDecimal;

public interface Accounting {

  final class Transfer {
    private Account from;
    private Account to;
    private BigDecimal amount;
    private AccountingEvent event;
    private Long sourceId;
    private String descr;
    private final Accounting accounting;

    public Transfer(Accounting accounting) {
      this.accounting = accounting;
    }

    public Transfer from(Account from) {
      this.from = from;
      return this;
    }

    public Transfer to(Account to) {
      this.to = to;
      return this;
    }

    public Transfer amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public Transfer event(AccountingEvent event) {
      this.event = event;
      return this;
    }

    public Transfer sourceId(Long sourceId) {
      this.sourceId = sourceId;
      return this;
    }

    public Transfer description(String descr) {
      this.descr = descr;
      return this;
    }

    public Transfer execute() {
      if (descr != null)
        this.accounting.transferMoney(from, to, amount, event, sourceId, descr);
      this.accounting.transferMoney(from, to, amount, event, sourceId);
      return this;
    }

  }


  Transfer newTransfer();

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

  void addOfferFunds(Offer offer, BigDecimal amount, Long sourceId);

  void addOfferFunds(Offer offer, BigDecimal amount);

}
