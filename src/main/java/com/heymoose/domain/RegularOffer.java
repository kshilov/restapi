package com.heymoose.domain;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.io.UnsupportedEncodingException;
import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("0")
public class RegularOffer extends Offer {
  
  @Basic
  private String description;
  
  @Basic
  private byte[] image;

  protected RegularOffer() {}
  public RegularOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, String description, String imageBase64) {
    super(title, url, autoApprove, creationTime, reentrant);
    checkNotNull(description, imageBase64);
    this.description = description;
    try {
      this.image = imageBase64.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public String description() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
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
}
