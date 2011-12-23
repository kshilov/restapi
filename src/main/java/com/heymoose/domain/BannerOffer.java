package com.heymoose.domain;

import static com.google.common.collect.Sets.newHashSet;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import static org.apache.commons.collections.SetUtils.unmodifiableSet;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("1")
public class BannerOffer extends Offer {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "offer_id", nullable = false)
  private Set<Banner> banners;

  protected BannerOffer() {}

  public BannerOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, Iterable<Banner> banners) {
    super(title, url, autoApprove, creationTime, reentrant);
    this.banners = newHashSet(banners);
  }

  public Iterable<Banner> banners() {
    return unmodifiableSet(banners);
  }

  public void addBanner(Banner banner) {
    checkNotNull(banner);
    if (banners == null)
      banners = newHashSet();
    banners.add(banner);
  }
}
