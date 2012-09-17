package com.heymoose.domain.accounting;

import com.heymoose.domain.base.BaseEntity;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "withdrawal_payment")
public final class WithdrawalPayment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "withdrawal-payment-seq")
  @SequenceGenerator(name = "withdrawal-payment-seq", sequenceName = "withdrawal_payment_seq", allocationSize = 1)
  private Long id;

  @Column(name = "withdrawal_id", nullable = false)
  private Long withdrawalId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Override
  public Long id() {
    return id;
  }

  public Long withdrawalId() {
    return withdrawalId;
  }

  public BigDecimal amount() {
    return amount;
  }

  public WithdrawalPayment setWithdrawalId(Long withdrawalId) {
    this.withdrawalId = withdrawalId;
    return this;
  }

  public WithdrawalPayment setAmount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  public WithdrawalPayment setCreationTime(DateTime date) {
    this.creationTime = date;
    return this;
  }
}
