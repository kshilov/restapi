package com.heymoose.domain;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.io.UnsupportedEncodingException;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("1")
public class BannerOffer extends Offer {

  @Basic
  private byte[] image;

  @ManyToOne
  @JoinColumn(name = "size")
  private BannerSize size;

  protected BannerOffer() {}

  public BannerOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, String imageBase64, BannerSize size) {
    super(title, url, autoApprove, creationTime, reentrant);
    checkNotNull(imageBase64);
    try {
      this.image = imageBase64.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    this.size = size;
  }

  public String imageBase64() {
    try {
      return new String(image, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public BannerSize size() {
    return size;
  }
}
