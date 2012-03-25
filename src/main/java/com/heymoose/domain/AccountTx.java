package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkState;
import com.heymoose.domain.base.IdEntity;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(
    name = "account_tx",
    uniqueConstraints = @UniqueConstraint(columnNames = {"version", "account_id"})
)
public class AccountTx extends IdEntity implements Comparable<AccountTx> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account-tx-seq")
  @SequenceGenerator(name = "account-tx-seq", sequenceName = "account_tx_seq", allocationSize = 1)
  private Long id;

  public Long id() {
    return id;
  }

  @Basic
  private Integer version;

  @Column(name = "parent_id")
  private Long parentId;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "from_account_id")
  private Account from;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(name = "to_account_id")
  private Account to;

  @Basic
  private BigDecimal balance;

  @Basic
  private BigDecimal amount;

  @Basic
  private BigDecimal diff;

  @Basic
  private String description;

  @Enumerated
  private TxType type;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = true)
  private DateTime creationTime;

  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "end_time", nullable = true)
  private DateTime endTime;

  @Enumerated(EnumType.ORDINAL)
  private AccountTxState state;

  protected AccountTx() {}

  public AccountTx(Account from, Account to, BigDecimal amount, TxType type) {
    this.from = from;
    this.to = to;
    this.amount = amount;
    this.type = type;
    this.creationTime = DateTime.now();
    this.state = AccountTxState.NOT_APPROVED;
  }

  public AccountTx(Account account, BigDecimal balance, TxType type) {
    this.account = account;
    this.to = account;
    this.balance = balance;
    account.setBalance(balance);
    this.diff = balance;
    this.amount = balance;
    this.version = 1;
    this.type = type;
    this.creationTime = DateTime.now();
    this.state = AccountTxState.APPROVED;
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

  public DateTime creationTime() {
    return creationTime;
  }

  public DateTime endTime() {
    return endTime;
  }
  
  public AccountTxState state() {
    return state;
  }

  public void approve() {
    checkState(state == AccountTxState.NOT_APPROVED);
    state = AccountTxState.APPROVED;
  }

  public AccountTx cancel() {
    checkState(state == AccountTxState.NOT_APPROVED);
    state = AccountTxState.CANCELED;
    return new AccountTx(to, from, amount, type);
  }

  public void addInPlace(BigDecimal amount, String description) {
    if (amount.signum() != 1)
      throw new IllegalArgumentException("Amount must be positive");
    version++;
    balance = balance.add(amount);
    account.setBalance(balance);
    this.description = description;
    diff = diff.add(amount);
    this.amount = diff;
    this.endTime = DateTime.now();
  }

  public AccountTx add(BigDecimal amount, String description, TxType type) {
    if (amount.signum() != 1)
      throw new IllegalArgumentException("Amount must be positive");
    AccountTx newAccount = new AccountTx();
    newAccount.account = this.account;
    newAccount.to = this.account;
    newAccount.version = this.version + 1;
    newAccount.balance = this.balance.add(amount);
    account.setBalance(newAccount.balance);
    newAccount.description = description;
    newAccount.diff = amount;
    newAccount.amount = amount;
    newAccount.parentId = this.id;
    newAccount.type = type;
    newAccount.creationTime = DateTime.now();
    return newAccount;
  }

  public AccountTx subtract(BigDecimal amount, String description, boolean allowNegativeBalance, TxType type) {
    if (amount.signum() != 1)
      throw new IllegalArgumentException("Amount must be positive");
    if (!allowNegativeBalance && balance.compareTo(amount) == -1)
      throw new IllegalStateException("No enough money");
    AccountTx newAccount = new AccountTx();
    newAccount.account = this.account;
    newAccount.from = this.account;
    newAccount.version = this.version + 1;
    newAccount.balance = this.balance.subtract(amount);
    account.setBalance(newAccount.balance);
    newAccount.description = description;
    newAccount.diff = amount.negate();
    newAccount.amount = amount;
    newAccount.parentId = this.id;
    newAccount.type = type;
    newAccount.creationTime = DateTime.now();
    return newAccount;
  }

  public TxType type() {
    if (type == null)
      return TxType.UNKNOWN;
    return type;
  }

  @Override
  public int compareTo(AccountTx o) {
    //TODO: WTF???
    return version.compareTo((o.version == null) ? 0 : o.version);
  }
}