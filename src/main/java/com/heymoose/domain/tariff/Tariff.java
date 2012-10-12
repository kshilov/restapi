package com.heymoose.domain.tariff;

import com.google.common.base.Preconditions;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.FeeType;
import com.heymoose.domain.offer.Offer;

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
import java.math.BigDecimal;

@Entity
@Table(name = "tariff")
public class Tariff extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tariff-seq")
  @SequenceGenerator(name = "tariff-seq", sequenceName = "tariff_seq", allocationSize = 1)
  protected Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @Column(name = "cpa_policy", nullable = false)
  @Enumerated(EnumType.STRING)
  private CpaPolicy cpaPolicy;

  @Column(name = "value", nullable = false)
  private BigDecimal value;

  @Enumerated(EnumType.STRING)
  @Column(name = "fee_type", nullable = false)
  protected FeeType feeType = FeeType.PERCENT;

  @Column(name = "fee", nullable = false)
  protected BigDecimal fee = new BigDecimal(30.0);

  @Override
  public Long id() {
    return this.id;
  }

  public CpaPolicy cpaPolicy() {
    return this.cpaPolicy;
  }

  public Tariff setCpaPolicy(CpaPolicy policy) {
    this.cpaPolicy = policy;
    return this;
  }

  public BigDecimal value() {
    return this.value;
  }

  public Tariff setValue(BigDecimal revenueValue) {
    this.value = revenueValue;
    return this;
  }


  public FeeType feeType() {
    return this.feeType;
  }

  public BigDecimal fee() {
    return this.fee;
  }

  public Offer offer() {
    return this.offer;
  }

  public Tariff setOffer(Offer offer) {
    this.offer = offer;
    return this;
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
        Preconditions.checkArgument(fee.compareTo(value) < 0,
            "Fee should be less than offers cost.");
        break;
    }
    this.fee = fee;
    return this;
  }

}
