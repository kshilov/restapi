package com.heymoose.domain.accounting;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.base.ModifiableEntity;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "accounting_entry")
public class AccountingEntry extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounting-entry-seq")
  @SequenceGenerator(name = "accounting-entry-seq", sequenceName = "accounting_entry_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY,optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  private Account account;

  @Basic(optional = false)
  private BigDecimal amount;

  @Basic
  private AccountingEvent event;

  @Column(name = "source_id")
  private Long sourceId;

  @Basic
  private String descr;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "transaction")
  private AccountingTransaction transaction;

  public AccountingEntry() { }

  public AccountingEntry(Account account, BigDecimal amount, AccountingEvent event, Long sourceId, String descr) {
    this(account, amount);
    this.event = event;
    this.sourceId = sourceId;
    this.descr = descr;
  }

  public AccountingEntry(Account account, BigDecimal amount) {
    checkArgument(amount.signum() != 0);
    this.account = account;
    this.amount = amount;
  }

  @Override
  public Long id() {
    return id;
  }

  public AccountingEntry setEvent(AccountingEvent event) {
    this.event = event;
    return this;
  }

  public AccountingEntry setTransaction(AccountingTransaction transaction) {
    this.transaction = transaction;
    return this;
  }

  public AccountingEntry setAmount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  public AccountingEntry setAccount(Account account) {
    this.account = account;
    return this;
  }

  public AccountingEntry setSourceId(Long sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public AccountingTransaction transaction() {
    return transaction;
  }

  public Account account() {
    return account;
  }

  public BigDecimal amount() {
    return amount;
  }
  
  public AccountingEvent event() {
    return event;
  }
  
  public String descr() {
    return descr;
  }

  @Override
  public String toString() {
    return "AccountingEntry{" +
        "id=" + id +
        ", account=" + account +
        ", amount=" + amount +
        ", event=" + event +
        ", sourceId=" + sourceId +
        ", descr='" + descr + '\'' +
        ", transaction=" + transaction +
        '}';
  }
}
