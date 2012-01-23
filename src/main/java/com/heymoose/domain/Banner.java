package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.io.UnsupportedEncodingException;
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

  @Basic
  private byte[] image;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "size")
  private BannerSize size;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "offer_id")
  private BannerOffer offer;

  protected Banner() {}

  public Banner(String imageBase64, String mimeType, BannerSize size) {
    checkNotNull(imageBase64, mimeType, size);
    try {
      this.image = imageBase64.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    this.mimeType = mimeType;
    this.size = size;
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

  public String imageBase64() {
    try {
      return new String(image, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void setImageBase64(String imageBase64) {
    try {
      this.image = imageBase64.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public BannerSize size() {
    return size;
  }

  public void setSize(BannerSize size) {
    checkNotNull(size);
    this.size = size;
  }

  public void setOffer(BannerOffer offer) {
    this.offer = offer;
  }

  public BannerOffer offer() {
    return offer;
  }
}
