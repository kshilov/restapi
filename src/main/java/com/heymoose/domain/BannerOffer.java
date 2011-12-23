package com.heymoose.domain;

import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("1")
public class BannerOffer extends Offer {

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Banner> banners;

  protected BannerOffer() {}

  public BannerOffer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, Iterable<Banner> banners) {
    super(title, url, autoApprove, creationTime, reentrant);
    this.banners = newHashSet(banners);
    for (Banner banner : banners)
      banner.setOffer(this);
  }

  public Iterable<Banner> banners() {
    return ImmutableSet.copyOf(banners);
  }

  public void addBanner(Banner banner) {
    checkNotNull(banner);
    if (banners == null)
      banners = newHashSet();
    for (Banner b : banners)
      if (b.size().equals(banner.size()))
        throw new IllegalArgumentException("Size already exists");
    banners.add(banner);
  }

  public void deleteBanner(Banner banner) {
    checkNotNull(banner);
    if (banners == null)
      banners = newHashSet();
    banners.remove(banner);
  }
}
