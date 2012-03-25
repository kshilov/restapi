package com.heymoose.domain.affiliate;

import com.heymoose.domain.Offer;
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
import org.hibernate.annotations.ManyToAny;
import org.joda.time.DateTime;
import sun.jvm.hotspot.bugspot.BugSpotAgent;

@Entity
@DiscriminatorValue("4")
public class SubOffer extends Offer {

  @Column(name = "parent_id", nullable = false)
  private Long parentId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  private NewOffer parent;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy", nullable = false)
  private CpaPolicy cpaPolicy;

  @Basic
  private BigDecimal cpa;

  @Basic
  private BigDecimal ratio;

  protected SubOffer() {}
  
  public SubOffer(Long parentId, CpaPolicy cpaPolicy, String title, String url, boolean autoApprove, boolean reentrant) {
    super(title, url, autoApprove, DateTime.now(), reentrant);
    checkNotNull(parentId, cpaPolicy);
    this.parentId = parentId;
    this.cpaPolicy = cpaPolicy;
  }
  
  public NewOffer parent() {
    return parent;
  }

  public CpaPolicy cpaPolicy() {
    return cpaPolicy;
  }

  public BigDecimal cpa() {
    return cpa;
  }
  
  public BigDecimal ratio() {
    return ratio;
  }
}
