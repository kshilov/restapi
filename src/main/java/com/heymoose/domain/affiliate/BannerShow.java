package com.heymoose.domain.affiliate;

import com.heymoose.domain.Banner;
import com.heymoose.domain.base.IdEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "banner_show")
public class BannerShow extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner-show-seq")
  @SequenceGenerator(name = "banner-show-seq", sequenceName = "banner_show_seq", allocationSize = 1)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "banner_id", insertable = false, updatable = false)
  private Banner banner;

  @ManyToOne(optional = false)
  @JoinColumn(name = "banner_id")
  private Long bannerId;

  @ManyToOne
  @JoinColumn(name = "site_id", insertable = false, updatable = false)
  public Site site;

  @ManyToOne(optional = false)
  @JoinColumn(name = "site_id")
  public Long siteId;

  @Override
  public Long id() {
    return id;
  }

  protected BannerShow() {}

  public BannerShow(long bannerId, long siteId) {
    this.bannerId = bannerId;
    this.siteId = siteId;
  }
}
