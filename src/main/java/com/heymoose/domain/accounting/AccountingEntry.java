package com.heymoose.domain.accounting;

import com.heymoose.domain.affiliate.base.ModifiableEntity;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

public class AccountingEntry extends ModifiableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounting-entry-seq")
  @SequenceGenerator(name = "accounting-entry-seq", sequenceName = "accounting_entry_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  private Account account;

  @Basic(optional = false)
  private BigDecimal amount;

  public AccountingEntry(Account account, BigDecimal amount) {
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
}
