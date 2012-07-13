package com.heymoose.domain.model.statistics;

import static com.google.common.base.Preconditions.checkArgument;

import com.heymoose.domain.model.Banner;
import com.heymoose.domain.model.User;
import com.heymoose.domain.model.base.BaseEntity;
import com.heymoose.domain.model.offer.Offer;
import com.heymoose.domain.model.offer.Subs;

import java.math.BigDecimal;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "offer_stat")
public class OfferStat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offer-stat-seq")
  @SequenceGenerator(name = "offer-stat-seq", sequenceName = "offer_stat_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "banner_id", insertable = false, updatable = false)
  @Nullable
  private Banner banner;

  @Column(name = "banner_id")
  @Nullable
  private Long bannerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offer_id", insertable = false, updatable = false)
  private Offer offer;

  @Column(name = "offer_id")
  private Long offerId;

  @Column(name = "master")
  private Long master;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "aff_id", insertable = false, updatable = false)
  private User affiliate;

  @Column(name = "aff_id")
  private Long affiliateId;

  @Column(name = "sub_id")
  @Nullable
  private String subId;

  @Column(name = "sub_id1")
  @Nullable
  private String subId1;

  @Column(name = "sub_id2")
  @Nullable
  private String subId2;

  @Column(name = "sub_id3")
  @Nullable
  private String subId3;

  @Column(name = "sub_id4")
  @Nullable
  private String subId4;

  @Column(name = "source_id")
  @Nullable
  private String sourceId;

  @Column(name = "show_count", nullable = false)
  private long showCount = 0;

  @Column(name = "click_count", nullable = false)
  private long clickCount = 0;

  @Column(name = "leads_count")
  private long leadsCount = 0;

  @Column(name = "sales_count")
  private long salesCount = 0;

  @Column(name = "not_confirmed_revenue")
  private BigDecimal notConfirmedRevenue = new BigDecimal(0);

  @Column(name = "confirmed_revenue")
  private BigDecimal confirmedRevenue = new BigDecimal(0);

  @Column(name = "canceled_revenue")
  private BigDecimal canceledRevenue = new BigDecimal(0);

  @Column(name = "referer")
  @Nullable
  private String referer;

  @Column(name = "keywords")
  @Nullable
  private String keywords;

  @Override
  public Long id() {
    return id;
  }

  protected OfferStat() {
  }

  public OfferStat(@Nullable Long bannerId, Long offerId, Long master, Long affId, String sourceId, Subs subs,
                   String referer, String keywords) {
    this.bannerId = bannerId;
    this.offerId = offerId;
    this.master = master;
    this.affiliateId = affId;
    this.subId = subs.subId();
    this.subId1 = subs.subId1();
    this.subId2 = subs.subId2();
    this.subId3 = subs.subId3();
    this.subId4 = subs.subId4();
    this.sourceId = sourceId;
    this.referer = referer;
    this.keywords = keywords;
  }

  public Long bannerId() {
    return bannerId;
  }

  public Offer offer() {
    return offer;
  }

  public User affiliate() {
    return affiliate;
  }

  public Subs subs() {
    return new Subs(subId, subId1, subId2, subId3, subId4);
  }

  public String sourceId() {
    return sourceId;
  }

  public String referer() {
    return referer;
  }

  public String keywords() {
    return keywords;
  }

  public void incClicks() {
    clickCount++;
  }

  public void incShows() {
    showCount++;
  }

  public void incLeads() {
    leadsCount++;
  }

  public void incSales() {
    salesCount++;
  }

  public void addToConfirmedRevenue(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (confirmedRevenue == null)
      confirmedRevenue = new BigDecimal(0);
    confirmedRevenue = confirmedRevenue.add(amount);
  }

  public void subtractFromConfirmedRevenue(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (confirmedRevenue == null)
      confirmedRevenue = new BigDecimal(0);
    confirmedRevenue = confirmedRevenue.subtract(amount);
  }

  public void addToNotConfirmedRevenue(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (notConfirmedRevenue == null)
      notConfirmedRevenue = new BigDecimal(0);
    notConfirmedRevenue = notConfirmedRevenue.add(amount);
  }

  public void approveMoney(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (notConfirmedRevenue == null)
      notConfirmedRevenue = new BigDecimal(0);
    if (confirmedRevenue == null)
      confirmedRevenue = new BigDecimal(0);
    notConfirmedRevenue = notConfirmedRevenue.subtract(amount);
    confirmedRevenue = confirmedRevenue.add(amount);
  }

  public void cancelMoney(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (notConfirmedRevenue == null)
      notConfirmedRevenue = new BigDecimal(0);
    if (canceledRevenue == null)
      canceledRevenue = new BigDecimal(0);
    notConfirmedRevenue = notConfirmedRevenue.subtract(amount);
    canceledRevenue = canceledRevenue.add(amount);
  }
}
