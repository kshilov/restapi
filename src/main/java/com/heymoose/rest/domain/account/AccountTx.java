package com.heymoose.rest.domain.account;

import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.order.Order;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.security.acl.Owner;

@Entity
@Table(
        name = "account_tx",
        uniqueConstraints = @UniqueConstraint(columnNames = {"version", "owner_id"})
)
public class AccountTx<T extends AccountOwner> extends IdEntity implements Comparable<AccountTx> {
  @Basic
  private Integer version;

  @Basic
  private Integer parentId;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, targetEntity = AccountOwner.class)
  @JoinColumn(name = "owner_id")
  private T owner;

  @Basic
  private BigDecimal balance;

  @Basic
  private BigDecimal diff;

  @Basic
  private String description;

  private AccountTx() {}

  public AccountTx(T owner, BigDecimal balance) {
    this.owner = owner;
    this.balance = balance;
    this.version = 1;
  }

  public T owner() {
    return owner;
  }

  public Integer version() {
    return version;
  }

  public Integer parentId() {
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
    newAccount.owner = this.owner;
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
      throw new IllegalArgumentException("No enough money");
    AccountTx newAccount = new AccountTx();
    newAccount.owner = this.owner;
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
