package com.heymoose.domain;

import java.io.UnsupportedEncodingException;
import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("0")
public class RegularOffer extends Offer {
  
  @Basic(optional = false)
  private String description;
  
  @Basic(optional = false)
  private byte[] image;

  protected RegularOffer() {}
  public RegularOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, String description, String imageBase64) {
    super(title, url, autoApprove, creationTime, reentrant);
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

  public String imageBase64() {
    try {
      return new String(image, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
