package com.heymoose.domain.accounting;

public class AccountingTransaction {

  private AccountingEntry src;
  private AccountingEntry dst;

  public AccountingTransaction(AccountingEntry src, AccountingEntry dst) {
    this.src = src;
    this.dst = dst;
  }
}
