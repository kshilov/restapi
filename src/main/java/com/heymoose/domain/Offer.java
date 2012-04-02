package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.base.IdEntity;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.joda.time.DateTime;

@Entity
@Table(name = "offer")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.INTEGER)
public class Offer extends IdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-seq")
  @SequenceGenerator(name = "offer-seq", sequenceName = "offer_seq", allocationSize = 1)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  protected User advertiser;
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id")
  protected Account account;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.LAZY, orphanRemoval = true)
  protected Set<SubOffer> suboffers;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<OfferGrant> grants;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "offer", fetch = FetchType.LAZY, orphanRemoval = true)
  protected Set<Banner> banners;
  @Enumerated(EnumType.STRING)
  @Column(name = "pay_method")
  protected PayMethod payMethod;
  @Enumerated(EnumType.STRING)
  @Column(name = "cpa_policy")
  protected CpaPolicy cpaPolicy;
  @Basic
  protected String name;
  @Basic
  protected String description;
  @Column(name = "logo_file_name")
  protected String logoFileName;
  @Basic
  protected BigDecimal cost;
  @Basic
  protected BigDecimal percent;
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
  @Basic
  protected boolean active;
  @Column(name = "block_reason")
  private String blockReason;
  @ManyToMany
  @JoinTable(
      name = "site_category",
      joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
  )
  private Set<Category> categories;

  public Long id() {
    return id;
  }

  @Basic(optional = false)
  private String title;

  @Basic(optional = false)
  private String url;

  @org.hibernate.annotations.Type(type = "org.joda.time.contrib.hibernate.PersistentDateTime")
  @Column(name = "creation_time", nullable = false)
  private DateTime creationTime;

  @Column(name = "auto_approve", nullable = false)
  private boolean autoApprove;

  @Basic(optional = false)
  private boolean reentrant;

  protected Offer() {}

  public Offer(String title, String url, boolean autoApprove, DateTime creationTime, boolean reentrant, BigDecimal cost, boolean allowNegativeBalance, User advertiser, String name, String description, CpaPolicy cpaPolicy, PayMethod payMethod, String logoFileName, BigDecimal percent) {
    checkNotNull(title, url, creationTime);
    this.title = title;
    this.url = url;
    this.autoApprove = autoApprove;
    this.creationTime = creationTime;
    this.reentrant = reentrant;
    this.cost = cost;
    this.account = new Account(allowNegativeBalance);
    this.advertiser = advertiser;
    this.name = name;
    this.approved = false;
    this.description = description;
    this.cpaPolicy = cpaPolicy;
    this.active = true;
    this.payMethod = payMethod;
    this.logoFileName = logoFileName;
    this.percent = percent;
  }

  public Offer(User advertiser, boolean allowNegativeBalance, String name, String description,
                  PayMethod payMethod, CpaPolicy cpaPolicy, BigDecimal cost, BigDecimal percent,
                  String title, String url, boolean autoApprove, boolean reentrant,
                  Iterable<Region> regions, String logoFileName) {

    this(title, url, autoApprove, DateTime.now(), reentrant, cost, allowNegativeBalance, advertiser, name, description, cpaPolicy, payMethod, logoFileName, percent);
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

    this.suboffers = newHashSet();
    this.banners = newHashSet();
    this.regions = newHashSet(regions);
  }

  public String title() {
    return title;
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

  public DateTime creationTime() {
    return creationTime;
  }

  public boolean autoApprove() {
    return autoApprove;
  }
  
  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public boolean reentrant() {
    return reentrant;
  }
  
  public void setReentrant(boolean reentrant) {
    this.reentrant = reentrant;
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
    banners.add(banner);
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
