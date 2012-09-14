package com.heymoose.domain.accounting;


import com.heymoose.domain.base.IdEntity;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "withdrawal")
public final class Withdrawal extends IdEntity {

  public static enum Basis { FEE, AFFILIATE_REVENUE }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "withdrawal-seq")
  @SequenceGenerator(name = "withdrawal-seq", sequenceName = "withdrawal_seq", allocationSize = 1)
  protected Long id;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "basis")
  @Enumerated(EnumType.STRING)
  private Basis basis;

  @Column(name = "source_id")
  private Long sourceId;

  @Column(name = "action_id")
  private Long actionId;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "creation_time")
  private DateTime creationTime;

  @Column(name = "order_time")
  private DateTime orderTime;

  @Override
  public Long id() {
    return this.id;
  }

  public Long userId() {
    return userId;
  }

  public Withdrawal setUserId(Long userId) {
    this.userId = userId;
    return this;
  }

  public Basis basis() {
    return basis;
  }

  public Withdrawal setBasis(Basis basis) {
    this.basis = basis;
    return this;
  }

  public Long sourceId() {
    return sourceId;
  }

  public Withdrawal setSourceId(Long sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public Long actionId() {
    return actionId;
  }

  public Withdrawal setActionId(Long actionId) {
    this.actionId = actionId;
    return this;
  }

  public BigDecimal amount() {
    return amount;
  }

  public Withdrawal setAmount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  public DateTime creationTime() {
    return creationTime;
  }

  public Withdrawal setCreationTime(DateTime creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public DateTime orderTime() {
    return orderTime;
  }

  public Withdrawal setOrderTime(DateTime orderTime) {
    this.orderTime = orderTime;
    return this;
  }
}
