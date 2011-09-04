package com.heymoose.domain;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
        name = "account_tx",
        uniqueConstraints = @UniqueConstraint(columnNames = {"version", "account_id"})
)
public class AccountTx extends IdEntity implements Comparable<AccountTx> {
  @Basic
  private Integer version;

  @Column(name = "parent_id")
  private Long parentId;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @Basic
  private BigDecimal balance;

  @Basic
  private BigDecimal diff;

  @Basic
  private String description;

  private AccountTx() {}

  public AccountTx(Account account, BigDecimal balance) {
    this.account = account;
    this.balance = balance;
    this.diff = balance;
    this.version = 1;
  }

  public Account account() {
    return account;
  }

  public Integer version() {
    return version;
  }

  public Long parentId() {
    return parentId;
  }

  public BigDecimal balance() {
    return balance;
  }

  public BigDecimal diff() {
    return diff;
  }

  public String description() {
    return description;
  }

  public AccountTx add(BigDecimal amount, String description) {
    if (amount.signum() != 1)
      throw new IllegalArgumentException("Amount must be positive");
    AccountTx newAccount = new AccountTx();
    newAccount.account = this.account;
    newAccount.version = this.version + 1;
    newAccount.balance = this.balance.add(amount);
    newAccount.description = description;
    newAccount.diff = amount;
    newAccount.parentId = this.id();
    return newAccount;
  }

  public AccountTx subtract(BigDecimal amount, String description) {
    if (amount.signum() != 1)
      throw new IllegalArgumentException("Amount must be positive");
    if (balance.compareTo(amount) == -1)
      throw new IllegalStateException("No enough money");
    AccountTx newAccount = new AccountTx();
    newAccount.account = this.account;
    newAccount.version = this.version + 1;
    newAccount.balance = this.balance.subtract(amount);
    newAccount.description = description;
    newAccount.diff = amount.negate();
    newAccount.parentId = this.id();
    return newAccount;
  }

  @Override
  public int compareTo(AccountTx o) {
    return version.compareTo(o.version);
  }
}
