package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Sets.newHashSet;

import com.heymoose.domain.Account;
import com.heymoose.domain.Banner;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("3")
public class NewOffer extends Offer {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User advertiser;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  private Account account;
  
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<Banner> banners;

  @Enumerated(EnumType.STRING)
  @Column(name = "pay_method", nullable = false)
  private PayMethod payMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy")
  private CpaPolicy cpaPolicy;
  
  @Basic
  private String name;

  @Basic
  private BigDecimal cost;
  
  @Basic
  private BigDecimal percent;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(
      name = "offer_region",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id")
  )
  @Column(name = "region")
  private Set<Region> regions;
  
  @Basic(optional = false)
  private boolean disabled = true;
  
  @Basic(optional = false)
  private boolean paused = false;

  protected NewOffer() {}

  public NewOffer(User advertiser, boolean allowNegativeBalance, String name,
                  PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                  String title, String url, boolean autoApprove, boolean reentrant,
                  Iterable<Region> regions) {
    
    super(title, url, autoApprove, DateTime.now(), reentrant);
    checkNotNull(advertiser, name, payMethod);
    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);
    
    if (payMethod == PayMethod.CPC || (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.FIXED)) {
      checkNotNull(cost);
      checkArgument(cost.signum() == 1);
    }
    else {
      checkNotNull(percent);
      checkArgument(percent.signum() == 1);
    }
    
    this.advertiser = advertiser;
    this.account = new Account(allowNegativeBalance);
    this.name = name;
    this.payMethod = payMethod;
    this.cpaPolicy = cpaPolicy;
    this.cost = cost;
    this.percent = percent;
    
    this.banners = newHashSet();
    this.regions = newHashSet(regions);
  }
  
  public User advertiser() {
    return advertiser;
  }
  
  public Account account() {
    return account;
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
  
  public String name() {
    return name;
  }
  
  public BigDecimal cost() {
    return cost;
  }
  
  public BigDecimal percent() {
    return percent;
  }
  
  public Set<Region> regions() {
    if (regions == null)
      return emptySet();
    return unmodifiableSet(regions());
  }
  
  public boolean disabled() {
    return disabled;
  }
  
  public boolean paused() {
    return paused;
  }
}
