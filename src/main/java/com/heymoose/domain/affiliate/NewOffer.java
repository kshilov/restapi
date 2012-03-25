package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Banner;
import com.heymoose.domain.City;
import com.heymoose.domain.Offer;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.util.Collections;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import org.hibernate.annotations.CollectionOfElements;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("3")
public class NewOffer extends Offer {

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<Banner> banners;

  @Enumerated(EnumType.STRING)
  @Column(name = "pay_method", nullable = false)
  private PayMethod payMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy")
  private CpaPolicy cpaPolicy;

  @Basic
  private BigDecimal cpa;

  @Basic
  private BigDecimal cpс;

  @Basic
  private BigDecimal ratio;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(
      name = "offer_region",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id")
  )
  @Column(name = "region")
  private Set<Region> regions;

  protected NewOffer() {}

  public NewOffer(CpaPolicy cpaPolicy, String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, Iterable<Region> regions, Iterable<Banner> banners) {
    super(title, url, autoApprove, creationTime, reentrant);
    checkNotNull(cpaPolicy);
    checkArgument(!isEmpty(banners));
    this.cpaPolicy = cpaPolicy;
    this.banners = newHashSet(banners);
    this.regions = newHashSet(regions);
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
    banner.setOffer(this);
  }

  public void deleteBanner(Banner banner) {
    checkNotNull(banner);
    if (banners.size() == 1)
      throw new IllegalArgumentException("Banner collection must contains at least two elements");
    banners.remove(banner);
  }

  public CpaPolicy cpaPolicy() {
    return cpaPolicy;
  }

  public PayMethod payMethod() {
    return payMethod;
  }
  
  public BigDecimal cpa() {
    return cpa;
  }
  
  public BigDecimal ratio() {
    return ratio;
  }
  
  public Set<Region> regions() {
    if (regions == null)
      return emptySet();
    return unmodifiableSet(regions());
  }
}
