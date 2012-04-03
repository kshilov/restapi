package com.heymoose.domain.affiliate;

import com.heymoose.domain.Banner;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.BaseEntity;
import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "click")
public class ClickStat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "click-seq")
  @SequenceGenerator(name = "click-seq", sequenceName = "click_seq", allocationSize = 1)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "banner_id", insertable = false, updatable = false)
  @Nullable
  private Banner banner;

  @Column(name = "banner_id")
  @Nullable
  private Long bannerId;

  @ManyToOne
  @JoinColumn(name = "offer_id", insertable = false, updatable = false)
  private Offer offer;

  @Column(name = "offer_id")
  private Long offerId;

  @ManyToOne
  @JoinColumn(name = "aff_id", insertable = false, updatable = false)
  private User affiliate;

  @Column(name = "aff_id")
  private Long affiliateId;

  @Column(name = "sub_id")
  @Nullable
  private String subId;

  @Column(name = "source_id")
  @Nullable
  private String sourceId;

  @Basic(optional = false)
  private long count = 1;

  @Override
  public Long id() {
    return id;
  }

  protected ClickStat(@Nullable Long bannerId, Long offerId, Long affId, @Nullable String subId,
                      @Nullable String sourceId) {
    this.bannerId = bannerId;
    this.offerId = offerId;
    this.affiliateId = affId;
    this.subId = subId;
    this.sourceId = sourceId;
  }
  
  public User affiliate() {
    return affiliate;
  }

  public String sourceId() {
    return sourceId;
  }

  public String subId () {
    return subId;
  }

  public void inc() {
    count++;
  }
}
