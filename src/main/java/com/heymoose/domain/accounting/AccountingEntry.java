package com.heymoose.domain.accounting;

import java.math.BigDecimal;

public class AccountingEntry {

  private final Account account;
  private final BigDecimal amount;
  private final AccountingEntryType type;
  private final Long source;
  private final String descr;

  public AccountingEntry(Account account, BigDecimal amount, AccountingEntryType type, Long source, String descr) {
    this.account = account;
    this.amount = amount;
    this.type = type;
    this.source = source;
    this.descr = descr;
  }
}
