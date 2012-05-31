package com.heymoose.domain.affiliate;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import com.heymoose.domain.User;
import com.heymoose.domain.accounting.Account;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.net.URI;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import java.util.SortedSet;
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
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@DiscriminatorValue("1")
public class Offer extends BaseOffer {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  protected User advertiser;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.LAZY, orphanRemoval = true)
  protected Set<SubOffer> suboffers;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<OfferGrant> grants;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  protected Set<Banner> banners;

  @Basic
  protected String name;

  @Basic
  protected String description;

  @Column(name = "logo_file_name")
  protected String logoFileName;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  @CollectionTable(
      name = "offer_region",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id")
  )
  @Column(name = "region")
  protected Set<Region> regions;

  @Basic
  protected boolean approved;

  @Column(name = "block_reason")
  private String blockReason;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  protected Account account;

  @ManyToMany
  @JoinTable(
      name = "offer_category",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
  )
  @Sort(type = SortType.NATURAL)
  private SortedSet<Category> categories;

  @Basic
  private String url;
  
  @Column(name = "site_url")
  private String siteUrl;

  @Column(name = "cookie_ttl")
  private int cookieTtl = 30;
  
  @Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "launch_time", nullable = true)
  private DateTime launchTime;

  @Column(name = "token_param_name", nullable = true)
  private String tokenParamName;

  protected Offer() {}

  public Offer(User advertiser, boolean allowNegativeBalance, String name, String description,
                  PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal cost2, BigDecimal percent,
                  String title, String url, String siteUrl, boolean autoApprove, boolean reentrant,
                  Iterable<Region> regions, Iterable<Category> categories, String logoFileName,
                  String code, int holdDays, int cookieTtl, DateTime launchTime) {

    super(payMethod, cpaPolicy, cost, cost2, percent, title, autoApprove, reentrant, code, holdDays);
    checkNotNull(url, siteUrl, advertiser, name, description, payMethod, cookieTtl, launchTime);
    this.url = url;
    this.siteUrl = siteUrl;
    this.advertiser = advertiser;
    this.name = name;
    this.approved = false;
    this.description = description;
    this.active = true;
    this.logoFileName = logoFileName;
    this.cookieTtl = cookieTtl;
    this.launchTime = launchTime;

    this.regions = newHashSet(regions);
    this.categories = newTreeSet(categories);
    this.account = new Account(allowNegativeBalance);
  }

  public String url() {
    return url;
  }
  
  public void setUrl(URI url) {
    this.url = url.toString();
  }
  
  public String siteUrl() {
    return siteUrl;
  }
  
  public void setSiteUrl(URI siteUrl) {
    this.siteUrl = siteUrl.toString();
  }

  public User advertiser() {
    return advertiser;
  }

  public Iterable<SubOffer> suboffers() {
    if (suboffers == null)
      emptySet();
    return unmodifiableSet(suboffers);
  }

  public Iterable<OfferGrant> grants() {
    if (grants == null)
      emptySet();
    return unmodifiableSet(grants);
  }

  public Iterable<Banner> banners() {
    if (banners == null)
      emptySet();
    return unmodifiableSet(banners);
  }

  public Set<Region> regions() {
    if (regions == null)
      return emptySet();
    return unmodifiableSet(regions);
  }
  
  public void setRegions(Iterable<Region> regions) {
    this.regions = newHashSet(regions);
  }

  public Set<Category> categories() {
    if (categories == null)
      return emptySet();
    return unmodifiableSet(categories);
  }
  
  public void setCategories(Iterable<Category> categories) {
    this.categories = newTreeSet(categories);
  }

  public void addBanner(Banner banner) {
    checkNotNull(banner);
    if (banners == null)
      banners = newHashSet();
    banners.add(banner);
  }

  public void deleteBanner(Banner banner) {
    checkNotNull(banner);
    banners.remove(banner);
  }

  public String name() {
    return name;
  }
  
  public void setName(String name) {
    checkNotNull(name);
    this.name = name;
  }

  public String description() {
    return description;
  }
  
  public void setDescription(String description) {
    checkNotNull(description);
    this.description = description;
  }

  public String logoFileName() {
    return logoFileName;
  }
  
  public void setLogoFileName(String logoFileName) {
    this.logoFileName = logoFileName;
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

  public String blockReason() {
    return blockReason;
  }

  public boolean visible() {
    return approved && active;
  }

  public Account account() {
    return account;
  }

  @Override
  public long master() {
    return id;
  }

  public void setCookieTtl(int cookieTtl) {
    this.cookieTtl = cookieTtl;
  }

  public int cookieTtl() {
    return cookieTtl;
  }
  
  public DateTime launchTime() {
    return launchTime;
  }
  
  public void setLaunchTime(DateTime launchTime) {
    this.launchTime = launchTime;
  }

  public String tokenParamName() {
    if (tokenParamName == null)
      return "_hm_token";
    return tokenParamName;
  }

  public void setTokenParamName(String tokenParamName) {
    this.tokenParamName = tokenParamName;
  }
  
  public Set<Long> subofferIds() {
    Set<Long> ids = newHashSet(id);
    for (SubOffer suboffer : suboffers)
      ids.add(suboffer.id());
    return ids;
  }
}
