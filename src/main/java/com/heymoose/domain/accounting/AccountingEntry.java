package com.heymoose.domain.accounting;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.affiliate.base.ModifiableEntity;
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

  protected AccountingEntry() {}

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
    this.account.setBalance(this.account.balance().add(amount));
  }

  @Override
  public Long id() {
    return id;
  }

  public AccountingEntry amend(BigDecimal amount) {
    this.amount = this.amount.add(amount);
    touch();
    return this;
  }

  public void setTransaction(AccountingTransaction transaction) {
    this.transaction = transaction;
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
}
