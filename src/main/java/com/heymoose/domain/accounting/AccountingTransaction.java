package com.heymoose.domain.accounting;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "accounting_transaction")
public class AccountingTransaction extends IdEntity  {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounting-transaction-seq")
  @SequenceGenerator(name = "accounting-transaction-seq", sequenceName = "accounting_transaction_seq", allocationSize = 1)
  private Long id;

  @Override
  public Long id() {
    return id;
  }

  @Override
  public String toString() {
    return "AccountingTransaction{" +
        "id=" + id +
        '}';
  }
}
