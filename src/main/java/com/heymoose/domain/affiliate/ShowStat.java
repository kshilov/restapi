package com.heymoose.domain.affiliate;

import com.heymoose.domain.Banner;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.BaseEntity;
import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "show_stat")
public class ShowStat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "show-stat-seq")
  @SequenceGenerator(name = "show-stat-seq", sequenceName = "show_stat_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "banner_id", insertable = false, updatable = false)
  @Nullable
  private Banner banner;

  @Column(name = "banner_id")
  @Nullable
  private Long bannerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", insertable = false, updatable = false)
  private Offer offer;

  @Column(name = "offer_id")
  private Long offerId;

  @ManyToOne(fetch = FetchType.LAZY)
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

  @Column(name = "show_count", nullable = false)
  private long count = 1;

  @Override
  public Long id() {
    return id;
  }

  protected ShowStat() {}

  public ShowStat(@Nullable Long bannerId, Long offerId, Long affId, @Nullable String subId,
                  @Nullable String sourceId) {
    this.bannerId = bannerId;
    this.offerId = offerId;
    this.affiliateId = affId;
    this.subId = subId;
    this.sourceId = sourceId;
  }

  public void inc() {
    count++;
  }
}
