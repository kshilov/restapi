package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "banner_click")
public class BannerClick extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner-click-seq")
  @SequenceGenerator(name = "banner-click-seq", sequenceName = "banner_click_seq", allocationSize = 1)
  private Long id;

  @Override
  public Long id() {
    return id;
  }

  protected BannerClick() {}
}
