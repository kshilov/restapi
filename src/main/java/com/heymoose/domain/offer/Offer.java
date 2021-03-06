package com.heymoose.domain.offer;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.site.Placement;
import com.heymoose.domain.user.User;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Sets.*;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;
import static java.util.Collections.*;

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
  private Set<Placement> placements;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  protected Set<Banner> banners;

  @Basic
  protected String name;

  @Basic
  protected String description;

  @Column(name = "short_description")
  protected String shortDescription;

  @Basic
  protected BigDecimal cr;

  @Basic
  protected boolean showcase;

  @Column(name = "logo_file_name")
  protected String logoFileName;

  @ElementCollection
  @CollectionTable(
      name = "offer_region",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id")
  )
  @Column(name = "region")
  protected Set<String> regions;

  @Basic
  protected boolean approved;

  @Column(name = "block_reason")
  private String blockReason;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  protected Account account;

  @Column(name = "is_product_offer")
  protected boolean isProductOffer;

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

  @Column(name = "allow_deeplink", nullable = true)
  protected Boolean allowDeeplink;

  @Column(name = "required_get_parameters", nullable = true)
  private String requiredGetParameters;

  @Column(name = "yml_url")
  private String ymlUrl;

  @Column(name = "allow_cashback")
  private boolean allowCashback;

  public Offer() {
  }

  public Offer(User advertiser, boolean allowNegativeBalance, String name, String description, String shortDescription,
               PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal cost2, BigDecimal percent,
               String title, String url, String siteUrl, boolean autoApprove, boolean reentrant,
               Iterable<String> regions, Iterable<Category> categories, String logoFileName,
               String code, int holdDays, int cookieTtl, DateTime launchTime, boolean allowDeeplink) {

    super(payMethod, cpaPolicy, cost, cost2, percent, title, autoApprove, reentrant, code, holdDays);
    checkNotNull(url, siteUrl, advertiser, name, description, shortDescription, payMethod, cookieTtl, launchTime);
    this.url = url;
    this.siteUrl = siteUrl;
    this.advertiser = advertiser;
    this.name = name;
    this.approved = false;
    this.description = description;
    this.shortDescription = shortDescription;
    this.showcase = false;
    this.active = true;
    this.logoFileName = logoFileName;
    this.cookieTtl = cookieTtl;
    this.launchTime = launchTime;

    this.regions = newHashSet(regions);
    this.categories = newTreeSet(categories);
    this.account = new Account(allowNegativeBalance);
    this.allowDeeplink = allowDeeplink;
  }

  public String url() {
    return url;
  }

  public Offer setUrl(URI url) {
    this.url = url.toString();
    return this;
  }

  public Offer setUrl(String url) {
    this.url = url;
    return this;
  }

  public Offer setAdvertiser(User advertiser) {
    this.advertiser = advertiser;
    return this;
  }

  public Offer addNewAccount(boolean allowNegativeBalance) {
    this.account = new Account(allowNegativeBalance);
    return this;
  }

  public String siteUrl() {
    return siteUrl;
  }

  public Offer setSiteUrl(URI siteUrl) {
    this.siteUrl = siteUrl.toString();
    return this;
  }

  public Offer setSiteUrl(String siteUrl) {
    this.siteUrl = siteUrl;
    return this;
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

  public Set<String> regions() {
    if (regions == null)
      return emptySet();
    return unmodifiableSet(regions);
  }

  public void setRegions(Iterable<String> regions) {
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

  public Offer setName(String name) {
    checkNotNull(name);
    this.name = name;
    return this;
  }

  public String description() {
    return description;
  }

  public Offer setDescription(String description) {
    checkNotNull(description);
    this.description = description;
    return this;
  }

  public String shortDescription() {
    return shortDescription;
  }

  public Offer setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
    return this;
  }

  public BigDecimal cr() {
    return cr;
  }

  public void setCr(BigDecimal cr) {
    this.cr = cr;
  }

  public boolean showcase() {
    return showcase;
  }

  public void setShowcase(boolean showcase) {
    this.showcase = showcase;
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

  @Override
  public Offer masterOffer() {
    return this;
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

  public Offer setTokenParamName(String tokenParamName) {
    this.tokenParamName = tokenParamName;
    return this;
  }

  public Set<Long> subofferIds() {
    Set<Long> ids = newHashSet(id);
    for (SubOffer suboffer : suboffers)
      ids.add(suboffer.id());
    return ids;
  }

  public Boolean allowDeeplink() {
    return allowDeeplink;
  }

  public void setAllowDeeplink(boolean allowDeeplink) {
    this.allowDeeplink = allowDeeplink;
  }

  public String requiredGetParameters() {
    return this.requiredGetParameters;
  }

  public Offer setRequiredGetParameters(String params) {
    this.requiredGetParameters = params;
    return this;
  }

  public boolean isProductOffer() {
    return this.isProductOffer;
  }

  public Offer setIsProductOffer(boolean productOffer) {
    this.isProductOffer = productOffer;
    return this;
  }

  public Offer setId(Long id) {
    this.id = id;
    return this;
  }

  public String ymlUrl() {
    return this.ymlUrl;
  }

  public Offer setYmlUrl(String ymlUrl) {
    this.ymlUrl = ymlUrl;
    return this;
  }


  @Override
  public Offer setCode(String code) {
    this.code = code;
    return this;
  }

  public Offer setAllowCashback(boolean allowCashback) {
    this.allowCashback = allowCashback;
    return this;
  }

  public boolean allowCashback() {
    return this.allowCashback;
  }
}
