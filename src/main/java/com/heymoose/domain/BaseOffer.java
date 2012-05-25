package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.base.BaseEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
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
  
  public void setCpaPolicy(CpaPolicy cpaPolicy) {
    this.cpaPolicy = cpaPolicy;
  }

  public BigDecimal cost() {
    return cost;
  }

  public BigDecimal cost2() {
    return cost2;
  }
  
  public void setCost(BigDecimal cost) {
    this.cost = cost;
    this.percent = null;
  }

  public void setCost2(BigDecimal cost2) {
    this.cost2 = cost2;
  }

  public BigDecimal percent() {
    return percent;
  }
  
  public void setPercent(BigDecimal percent) {
    this.percent = percent;
    this.cost = null;
  }

  public boolean active() {
    return active;
  }

  public PayMethod payMethod() {
    return payMethod;
  }
  
  public void setPayMethod(PayMethod payMethod) {
    this.payMethod = payMethod;
  }

  public String title() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }

  public boolean autoApprove() {
    return autoApprove;
  }

  public boolean reentrant() {
    return reentrant;
  }

  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public void setReentrant(boolean reentrant) {
    this.reentrant = reentrant;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
  
  public String code() {
    return code;
  }
  
  public void setCode(String code) {
    this.code = code;
  }

  public int holdDays() {
    return holdDays;
  }

  public void setHoldDays(int holdDays) {
    checkArgument(holdDays >= 0 && holdDays <= 180);
    this.holdDays = holdDays;
  }

  public abstract Account account();

  public abstract long master();
}
