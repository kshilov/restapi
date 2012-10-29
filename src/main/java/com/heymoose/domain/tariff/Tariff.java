package com.heymoose.domain.tariff;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.FeeType;
import com.heymoose.domain.product.Product;

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

  private static final BigDecimal HUNDRED = new BigDecimal(100);
  public static final FeeType DEFAULT_FEE_TYPE = FeeType.PERCENT;
  public static final BigDecimal DEFAULT_FEE = new BigDecimal("30.0");

  public static Tariff forValue(CpaPolicy policy, BigDecimal value) {
    return new Tariff().setValue(policy, value);
  }
  public static Tariff forOffer(BaseOffer offer) {
    return new Tariff().setOffer(offer);
  }

  public static Tariff forProduct(Product product) {
    return new Tariff().setOffer(product.offer());
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tariff-seq")
  @SequenceGenerator(name = "tariff-seq", sequenceName = "tariff_seq", allocationSize = 1)
  protected Long id;


  @ManyToOne
  @JoinColumn(name = "offer_id")
  private BaseOffer offer;

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

  @Column(nullable = false)
  private boolean exclusive = false;

  @Override
  public Long id() {
    return this.id;
  }

  public BaseOffer offer() {
    return this.offer;
  }

  public Tariff setOffer(BaseOffer offer) {
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
    this.feeType = type;
    return this;
  }

  public Tariff setFee(BigDecimal fee) {
    this.fee = fee;
    return this;
  }

  public boolean exclusive() {
    return this.exclusive;
  }

  public Tariff setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
    return this;
  }


  public BigDecimal percentOf(BigDecimal amount) {
    Preconditions.checkState(cpaPolicy() == CpaPolicy.PERCENT,
        "Wrong CpaPolicy");
    Preconditions.checkNotNull(this.percent, "No percent given.");
    Preconditions.checkNotNull(amount, "No amount given.");
    return this.percent().divide(HUNDRED).multiply(amount);
  }

  public BigDecimal affiliatePart(BigDecimal amount) {
    switch (this.feeType) {
      case PERCENT:
        // cost = aff_part + our_part
        // our_part =  aff_part * (our_fee / 100%)
        // our_fee = offer.fee()
        // cost = aff_part + aff_part * offer.fee() / 100%
        // aff_part = cost / (1 + offer.fee() / 100%)
        BigDecimal divider = this.fee
            .divide(HUNDRED)
            .add(BigDecimal.ONE);
        return amount.divide(divider, 2, BigDecimal.ROUND_UP);
      case FIX:
        // cost = aff_part + our_part
        // our_part = offer.fee()
        // aff_part = cost - offer.fee()
        return amount.subtract(this.fee);
    }
    throw new RuntimeException("Unknown FeeType: " + this.feeType);
  }

  public BigDecimal heymoosePart(BigDecimal amount) {
    return amount.subtract(affiliatePart(amount));
  }


  /**
   * Returns affiliate revenue, if it is fixed or can be
   * calculated, using data in tariff. Returns null otherwise.
   *
   * @return fixed affiliate revenue or null
   */
  public BigDecimal affiliateCost() {
    if (cpaPolicy != null && cpaPolicy == CpaPolicy.PERCENT)
      return null; // affiliatePercent() should not return null in this case
    BigDecimal cost = this.cost;
    if (this.cpaPolicy == CpaPolicy.DOUBLE_FIXED) cost = this.firstActionCost;
    switch (feeType) {
      case FIX:
        return cost.subtract(fee);
      case PERCENT:
        BigDecimal divider = fee
            .divide(new BigDecimal(100))
            .add(BigDecimal.ONE);
        return cost.divide(divider, 2, BigDecimal.ROUND_UP);
    }
    throw new RuntimeException("Unknown fee type for offer " + id);
  }

  /**
   * Returns affiliate revenue for repeated action in case of
   * {@link CpaPolicy.DOUBLE_FIXED} policy or null otherwise.
   *
   * @return affiliate revenue for repeated action or null
   */
  public BigDecimal affiliateCost2() {
    if (cpaPolicy == null || cpaPolicy != CpaPolicy.DOUBLE_FIXED)
      return null;
    BigDecimal divider = fee
        .divide(new BigDecimal(100))
        .add(BigDecimal.ONE);
    return otherActionCost.divide(divider, 2, BigDecimal.ROUND_UP);
  }

  /**
   * Returns affiliate revenue, in case of {@link CpaPolicy.PERCENT},
   * null otherwise.
   *
   * @return affiliate revenue for {@link CpaPolicy.PERCENT} offers
   */
  public BigDecimal affiliatePercent() {
    if (cpaPolicy == null || cpaPolicy != CpaPolicy.PERCENT)
      return null; // affiliateCost() should not return null in this case
    if (feeType == FeeType.FIX)
      throw new IllegalStateException("Wrong feeType");
    BigDecimal divider = fee
        .divide(new BigDecimal(100))
        .add(BigDecimal.ONE);
    return percent.divide(divider, 2, BigDecimal.ROUND_UP);
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

  public BigDecimal value() {
    switch (cpaPolicy) {
      case FIXED: return cost;
      case PERCENT: return percent;
      default: throw new IllegalStateException("Wrong policy " + cpaPolicy);
    }
  }

  public BigDecimal affiliateValue() {
    switch (cpaPolicy) {
      case FIXED: return affiliateCost();
      case PERCENT: return affiliatePercent();
      default: return null;
    }
  }
}
