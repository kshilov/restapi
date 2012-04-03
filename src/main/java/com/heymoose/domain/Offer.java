package com.heymoose.domain;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.SubOffer;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
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

@Entity
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
  private Set<Category> categories;

  @Basic(optional = false)
  private String url;

  protected Offer() {}

  public Offer(User advertiser, boolean allowNegativeBalance, String name, String description,
                  PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                  String title, String url, boolean autoApprove, boolean reentrant,
                  Iterable<Region> regions, Iterable<Category> categories, String logoFileName) {

    super(payMethod, cpaPolicy, cost, percent, title, autoApprove, reentrant);
    checkNotNull(url, advertiser, name, description, payMethod);
    this.url = url;
    this.advertiser = advertiser;
    this.name = name;
    this.approved = false;
    this.description = description;
    this.active = true;
    this.logoFileName = logoFileName;

    this.regions = newHashSet(regions);
    this.categories = newHashSet(categories);
    this.account = new Account(allowNegativeBalance);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String url() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
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

  public Set<Category> categories() {
    if (categories == null)
      return emptySet();
    return unmodifiableSet(categories);
  }

  public void addBanner(Banner banner) {
    checkNotNull(banner);
    if (banners == null)
      banners = newHashSet();
    banners.add(banner);
  }

  public void deleteBanner(Banner banner) {
    checkNotNull(banner);
    if (banners.size() == 1)
      throw new IllegalArgumentException("Banner collection must contains at least two elements");
    banners.remove(banner);
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
}
