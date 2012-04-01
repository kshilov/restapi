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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
  
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<SubOffer> suboffers;
  
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<OfferGrant> grants;
  
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<Banner> banners;

  @Enumerated(EnumType.STRING)
  @Column(name = "pay_method")
  private PayMethod payMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy")
  private CpaPolicy cpaPolicy;
  
  @Basic
  private String name;
  
  @Basic
  private String description;
  
  @Column(name = "logo_file_name")
  private String logoFileName;

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
  
  @Basic
  private boolean approved;
  
  @Basic
  private boolean active;
  
  @Column(name = "block_reason")
  private String blockReason;

  @ManyToMany
  @JoinTable(
      name = "site_category",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
  )
  private Set<Category> categories;

  protected NewOffer() {}

  public NewOffer(User advertiser, boolean allowNegativeBalance, String name, String description,
                  PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                  String title, String url, boolean autoApprove, boolean reentrant,
                  Iterable<Region> regions, String logoFileName) {
    
    super(title, url, autoApprove, DateTime.now(), reentrant);
    checkNotNull(advertiser, name, description, payMethod);
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
    this.description = description;
    this.logoFileName = logoFileName;
    this.payMethod = payMethod;
    this.cpaPolicy = cpaPolicy;
    this.cost = cost;
    this.percent = percent;
    this.approved = false;
    this.active = true;
    
    this.suboffers = newHashSet();
    this.banners = newHashSet();
    this.regions = newHashSet(regions);
  }
  
  public User advertiser() {
    return advertiser;
  }
  
  public Account account() {
    return account;
  }
  
  public Iterable<SubOffer> suboffers() {
    return ImmutableSet.copyOf(suboffers);
  }
  
  public Iterable<OfferGrant> grants() {
    return ImmutableSet.copyOf(grants);
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
  
  public String description() {
    return description;
  }
  
  public String logoFileName() {
    return logoFileName;
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
    return unmodifiableSet(regions);
  }
  
  public boolean approved() {
    return approved;
  }
  
  public void setApproved(boolean approved) {
    this.approved = approved;
  }
  
  public void block(String reason) {
    this.approved = false;
    this.blockReason = reason;
  }
  
  public void unblock() {
    this.approved = true;
  }
  
  public boolean active() {
    return active;
  }
  
  public void setActive(boolean active) {
    this.active = active;
  }
  
  public String blockReason() {
    return blockReason;
  }
  
  public boolean visible() {
    return approved && active;
  }
}
