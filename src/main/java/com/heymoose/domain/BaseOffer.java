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
  @Column(name = "cpa_policy", nullable = false)
  protected CpaPolicy cpaPolicy;

  @Basic
  protected BigDecimal cost;

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

  protected BaseOffer() {}

  public BaseOffer(PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                   String title, boolean autoApprove, boolean reentrant) {

    checkNotNull(payMethod, cpaPolicy);
    BigDecimal valueToCheck = cpaPolicy == CpaPolicy.FIXED ? cost : percent;
    checkNotNull(valueToCheck);
    checkArgument(valueToCheck.signum() == 1);

    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);

    if (payMethod == PayMethod.CPC || (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.FIXED)) {
      checkNotNull(cost);
      checkArgument(cost.signum() == 1);
    }
    else {
      checkNotNull(percent);
      checkArgument(percent.signum() == 1);
    }

    this.payMethod = payMethod;
    this.cpaPolicy = cpaPolicy;
    this.cost = cost;
    this.percent = percent;

    this.title = title;
    this.autoApprove = autoApprove;
    this.reentrant = reentrant;
  }

  @Override
  public Long id() {
    return id;
  }

  public CpaPolicy cpaPolicy() {
    return cpaPolicy;
  }

  public BigDecimal cost() {
    return cost;
  }

  public BigDecimal percent() {
    return percent;
  }

  public boolean active() {
    return active;
  }

  public PayMethod payMethod() {
    return payMethod;
  }

  public String title() {
    return title;
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

  public abstract Account account();
}
