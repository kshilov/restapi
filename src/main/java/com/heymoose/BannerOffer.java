package com.heymoose;

import com.heymoose.domain.Offer;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.io.UnsupportedEncodingException;
import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("1")
public class BannerOffer extends Offer {

  @Basic
  private byte[] image;

  protected BannerOffer() {}

  public BannerOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, String imageBase64) {
    super(title, url, autoApprove, creationTime, reentrant);
    checkNotNull(imageBase64);
    try {
      this.image = imageBase64.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public String imageBase64() {
    try {
      return new String(image, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
