package com.heymoose.domain.accounting;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

public class AccountingTransaction extends IdEntity  {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounting-tx-seq")
  @SequenceGenerator(name = "accounting-tx-seq", sequenceName = "accounting_tx_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "src")
  private AccountingEntry src;

  @ManyToOne(optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "dst")
  private AccountingEntry dst;

  @Enumerated
  private AccountingTransactionType type;

  @Basic
  private Long event;

  @Basic
  private String descr;

  public AccountingTransaction(AccountingEntry src, AccountingEntry dst, AccountingTransactionType type, Long event, String descr) {
    this.src = src;
    this.dst = dst;
    this.event = event;
    this.descr = descr;
  }

  @Override
  public Long id() {
    return id;
  }
}
