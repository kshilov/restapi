package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;

import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.annotation.Nullable;
import javax.persistence.Basic;
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
@Table(name = "banner")
public class Banner extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner-seq")
  @SequenceGenerator(name = "banner-seq", sequenceName = "banner_seq", allocationSize = 1)
  private Long id;

  @Basic
  private String mimeType;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private Offer offer;

  @Basic
  private Integer width;

  @Basic
  private Integer height;

  @Basic
  private String url;

  protected Banner() {}

  public Banner(Offer offer, String mimeType, Integer width, Integer height, @Nullable String url) {
    checkNotNull(offer, mimeType);
    checkArgument((width == null && height == null) || (width != null && height !=null));
    this.offer = offer;
    this.width = width;
    this.height = height;
    this.mimeType = mimeType;
    this.url = url;
  }

  @Override
  public Long id() {
    return id;
  }

  public String mimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  public Offer offer() {
    return offer;
  }
  
  public Integer width() {
    return width;
  }
  
  public Integer height() {
    return height;
  }

  public String url() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
}