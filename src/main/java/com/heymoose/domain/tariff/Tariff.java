package com.heymoose.domain.tariff;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.FeeType;
import com.heymoose.domain.offer.Offer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tariff")
public class Tariff extends IdEntity {

  public static final FeeType DEFAULT_FEE_TYPE = FeeType.PERCENT;
  public static final BigDecimal DEFAULT_FEE = new BigDecimal("30.0");

  public static Tariff forValue(CpaPolicy policy, BigDecimal value) {
    return new Tariff().setValue(policy, value);
  }
  public static Tariff forOffer(Offer offer) {
    return new Tariff().setOffer(offer);
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tariff-seq")
  @SequenceGenerator(name = "tariff-seq", sequenceName = "tariff_seq", allocationSize = 1)
  protected Long id;


  @ManyToOne
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @Column(name = "cpa_policy", nullable = false)
  @Enumerated(EnumType.STRING)
  private CpaPolicy cpaPolicy;

  @Column
  private BigDecimal cost;

  @Column
  private BigDecimal percent;

  @Column(name = "first_action_cost")
  private BigDecimal firstActionCost;

  @Column(name = "other_action_cost")
  private BigDecimal otherActionCost;

  @Enumerated(EnumType.STRING)
  @Column(name = "fee_type", nullable = false)
  protected FeeType feeType = DEFAULT_FEE_TYPE;

  @Column(name = "fee", nullable = false)
  protected BigDecimal fee = DEFAULT_FEE;

  @Override
  public Long id() {
    return this.id;
  }

  public Offer offer() {
    return this.offer;
  }

  public Tariff setOffer(Offer offer) {
    this.offer = offer;
    return this;
  }

  public CpaPolicy cpaPolicy() {
    return this.cpaPolicy;
  }

  public Tariff setCpaPolicy(CpaPolicy policy) {
    this.cpaPolicy = policy;
    return this;
  }

  public BigDecimal cost() {
    return this.cost;
  }

  public Tariff setCost(BigDecimal cost) {
    this.cost = cost;
    return this;
  }

  public BigDecimal percent() {
    return this.percent;
  }

  public Tariff setPercent(BigDecimal percent) {
    this.percent = percent;
    return this;
  }

  public BigDecimal firstActionCost() {
    return this.firstActionCost;
  }

  public Tariff setFirstActionCost(BigDecimal cost) {
    this.firstActionCost = cost;
    return this;
  }

  public BigDecimal otherActionCost() {
    return this.otherActionCost;
  }

  public Tariff setOtherActionCost(BigDecimal cost) {
    this.otherActionCost = cost;
    return this;
  }

  public Tariff setValue(CpaPolicy policy, BigDecimal value) {
    switch (policy) {
      case PERCENT:
        this.percent = value;
        break;
      case FIXED:
        this.cost = value;
        break;
      default:
        throw new IllegalArgumentException("Not valid policy: " + policy);
    }
    this.cpaPolicy = policy;
    return this;
  }


  public FeeType feeType() {
    return this.feeType;
  }

  public BigDecimal fee() {
    return this.fee;
  }

  public Tariff setFeeType(FeeType type) {
    if (cpaPolicy == CpaPolicy.PERCENT) {
      Preconditions.checkArgument(type == FeeType.PERCENT,
          "FeeType should be percent for percent offers.");
    }
    this.feeType = type;
    return this;
  }

  public Tariff setFee(BigDecimal fee) {
    switch (feeType) {
      case PERCENT:
        double feeDouble = fee.doubleValue();
        Preconditions.checkArgument(feeDouble > 0,
            "Fee should be more than 0%.");
        Preconditions.checkArgument(feeDouble < 100,
            "Fee should be less than 100%.");
        break;
      case FIX:
        Preconditions.checkArgument(fee.compareTo(cost) < 0,
            "Fee should be less than offers cost.");
        break;
    }
    this.fee = fee;
    return this;
  }

  @Override
  public String toString() {
    Objects.ToStringHelper builder = Objects.toStringHelper(Tariff.class)
        .add("id", id)
        .add("feeType", feeType)
        .add("fee", fee);
    switch (cpaPolicy) {
      case FIXED:
        builder.add("cost", cost);
        break;
      case PERCENT:
        builder.add("percent", percent);
        break;
      case DOUBLE_FIXED:
        builder.add("firstActionCost", firstActionCost);
        builder.add("otherActionCost", otherActionCost);
        break;
    }
    return builder.toString();
  }

}
