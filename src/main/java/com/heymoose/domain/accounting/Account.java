package com.heymoose.domain.accounting;

import java.math.BigDecimal;

public class Account {

  private BigDecimal balance;

  public Account(BigDecimal balance) {
    this.balance = balance;
  }

  public BigDecimal balance() {
    return balance;
  }
}
