package com.heymoose.domain.affiliate;

import com.heymoose.domain.base.IdEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "banner_action")
public class BannerAction extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner-action-seq")
  @SequenceGenerator(name = "banner-action-seq", sequenceName = "banner_action_seq", allocationSize = 1)
  private Long id;

  @Override
  public Long id() {
    return id;
  }

  protected BannerAction() {}
}
