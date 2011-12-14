package com.heymoose.domain;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.net.URI;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.ws.rs.WebApplicationException;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("2")
public class VideoOffer extends Offer {

  @Column(name = "video_url")
  private String videoUrl;

  protected VideoOffer() {}

  public VideoOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, String videoUrl) {
    super(title, url, autoApprove, creationTime, reentrant);
    checkNotNull(videoUrl);
    try {
      URI.create(videoUrl);
    } catch (Exception e) {
      throw new WebApplicationException(400);
    }
    this.videoUrl = videoUrl;
  }

  public String videoUrl() {
    return videoUrl;
  }
  
  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
  }
}
