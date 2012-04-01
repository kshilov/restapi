package com.heymoose.domain.affiliate;

import com.heymoose.domain.Offer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("4")
public class SubOffer extends Offer {

  @Column(name = "parent_id")
  private Long parentId;

  @ManyToOne()
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private NewOffer parent;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy", nullable = false)
  private CpaPolicy cpaPolicy;

  @Basic
  private BigDecimal cost;

  @Basic
  private BigDecimal percent;
  
  @Basic
  private boolean active = false;

  protected SubOffer() {}
  
  public SubOffer(Long parentId, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                  String title, boolean autoApprove, boolean reentrant) {
    super(title, "", autoApprove, DateTime.now(), reentrant);
    checkNotNull(parentId, cpaPolicy);
    
    BigDecimal valueToCheck = cpaPolicy == CpaPolicy.FIXED ? cost : percent;
    checkNotNull(valueToCheck);
    checkArgument(valueToCheck.signum() == 1);
    
    this.parentId = parentId;
    this.cpaPolicy = cpaPolicy;
    this.cost = cost;
    this.percent = percent;
  }
  
  public Long parentId() {
    return parentId;
  }
  
  public NewOffer parent() {
    return parent;
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
}
