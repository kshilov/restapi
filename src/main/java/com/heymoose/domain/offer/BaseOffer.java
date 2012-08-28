package com.heymoose.domain.offer;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.base.BaseEntity;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "offer")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
public abstract class BaseOffer extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-seq")
  @SequenceGenerator(name = "offer-seq", sequenceName = "offer_seq", allocationSize = 1)
  protected Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "pay_method")
  protected PayMethod payMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy")
  protected CpaPolicy cpaPolicy;

  @Basic
  protected BigDecimal cost;

  @Basic
  protected BigDecimal cost2;

  @Basic
  protected BigDecimal percent;

  @Enumerated(EnumType.STRING)
  @Column(name = "fee_type", nullable = false)
  protected FeeType feeType = FeeType.PERCENT;

  @Column(name = "fee", nullable = false)
  protected BigDecimal fee = new BigDecimal(30.0);

  @Column(name = "item_price", nullable = true)
  protected BigDecimal itemPrice;

  @Basic
  protected boolean active = false;

  @Basic(optional = false)
  protected String title;

  @Column(name = "auto_approve", nullable = false)
  protected boolean autoApprove;

  @Basic(optional = false)
  protected boolean reentrant;

  @Basic(optional = false)
  private String code;

  @Column(name = "hold_days")
  private int holdDays;

  @Column(name = "exclusive", nullable = false)
  private boolean exclusive;

  protected BaseOffer() {}

  public BaseOffer(PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal cost2, BigDecimal percent,
                   String title, boolean autoApprove, boolean reentrant, String code, int holdDays) {

    checkNotNull(payMethod);
    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);

    if (payMethod == PayMethod.CPC || (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.FIXED)) {
      checkNotNull(cost);
      checkArgument(cost.signum() == 1);
    } else if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.DOUBLE_FIXED) {
      checkNotNull(cost2);
      checkArgument(cost2.signum() == 1);
    } else {
      checkNotNull(percent);
      checkArgument(percent.signum() == 1);
    }

    this.payMethod = payMethod;
    this.cpaPolicy = cpaPolicy;
    this.cost = cost;
    this.cost2 = cost2;
    this.percent = percent;

    this.title = title;
    this.autoApprove = autoApprove;
    this.reentrant = reentrant;
    this.code = code;
    this.holdDays = holdDays;
  }

  @Override
  public Long id() {
    return id;
  }

  public CpaPolicy cpaPolicy() {
    return cpaPolicy;
  }
  
  public BaseOffer setCpaPolicy(CpaPolicy cpaPolicy) {
    this.cpaPolicy = cpaPolicy;
    return this;
  }

  public BigDecimal cost() {
    return cost;
  }

  public BigDecimal cost2() {
    return cost2;
  }
  
  public BaseOffer setCost(BigDecimal cost) {
    this.cost = cost;
    return this;
  }

  public BaseOffer setCost2(BigDecimal cost2) {
    this.cost2 = cost2;
    return this;
  }

  public BigDecimal percent() {
    return percent;
  }
  
  public BaseOffer setPercent(BigDecimal percent) {
    this.percent = percent;
    return this;
  }

  public BaseOffer setItemPrice(BigDecimal itemPrice) {
    this.itemPrice = itemPrice;
    return this;
  }

  public FeeType feeType() {
    return this.feeType;
  }

  public BigDecimal fee() {
    return this.fee;
  }

  public BaseOffer setFeeType(FeeType type) {
    if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.PERCENT) {
      Preconditions.checkArgument(
          type == FeeType.PERCENT,
          "FeeType should be percent for percent offers.");
    }
    this.feeType = type;
    return this;
  }

  public BaseOffer setFee(BigDecimal fee) {
    switch (feeType) {
      case PERCENT:
        double feeDouble = fee.doubleValue();
        Preconditions.checkArgument(
            feeDouble > 0, "Fee should be more than 0%.");
        Preconditions.checkArgument(
            feeDouble < 100, "Fee should be less than 100%.");
        break;
      case FIX:
        Preconditions.checkArgument(
            fee.compareTo(cost) < 0, "Fee should be less than offers cost.");
        break;
    }
    this.fee = fee;
    return this;
  }

  /**
   * Returns affiliate revenue, if it is fixed or can be
   * calculated, using data in offer. Returns null otherwise.
   *
   * @return fixed affiliate revenue or null
   */
  public BigDecimal affiliateCost() {
    if (cpaPolicy != null && cpaPolicy == CpaPolicy.PERCENT)
      return null; // affiliatePercent() should not return null in this case
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
    return cost2.divide(divider, 2, BigDecimal.ROUND_UP);
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
    BigDecimal divider = fee
        .divide(new BigDecimal(100))
        .add(BigDecimal.ONE);
    return percent.divide(divider, 2, BigDecimal.ROUND_UP);
  }

  public BigDecimal itemPrice() {
    return this.itemPrice;
  }

  public boolean exclusive() {
    return this.exclusive;
  }

  public BaseOffer setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
    return this;
  }

  public boolean active() {
    return active;
  }

  public PayMethod payMethod() {
    return payMethod;
  }
  
  public BaseOffer setPayMethod(PayMethod payMethod) {
    this.payMethod = payMethod;
    return this;
  }

  public String title() {
    return title;
  }
  
  public BaseOffer setTitle(String title) {
    this.title = title;
    return this;
  }

  public boolean autoApprove() {
    return autoApprove;
  }

  public boolean reentrant() {
    return reentrant;
  }

  public BaseOffer setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
    return this;
  }

  public BaseOffer setReentrant(boolean reentrant) {
    this.reentrant = reentrant;
    return this;
  }

  public BaseOffer setActive(boolean active) {
    this.active = active;
    return this;
  }
  
  public String code() {
    return code;
  }
  
  public BaseOffer setCode(String code) {
    this.code = code;
    return this;
  }

  public int holdDays() {
    return holdDays;
  }

  public BaseOffer setHoldDays(int holdDays) {
    checkArgument(holdDays >= 0 && holdDays <= 180);
    this.holdDays = holdDays;
    return this;
  }

  public abstract Account account();

  public abstract long master();

  public abstract Set<String> regions();
}
