package com.heymoose.domain.accounting;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class AccountingTransaction extends IdEntity  {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounting-tx-seq")
  @SequenceGenerator(name = "accounting-tx-seq", sequenceName = "accounting_tx_seq", allocationSize = 1)
  private Long id;

  @Override
  public Long id() {
    return id;
  }
}
