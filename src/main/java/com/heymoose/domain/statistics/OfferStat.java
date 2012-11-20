package com.heymoose.domain.statistics;

import com.heymoose.domain.base.BaseEntity;
import com.heymoose.domain.offer.Banner;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.user.User;

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
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

@Entity
@Table(name = "offer_stat")
public class OfferStat extends BaseEntity {

  private static final BigDecimal ZERO = new BigDecimal(0);

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
  private BaseOffer offer;

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
  private BigDecimal notConfirmedRevenue = ZERO;

  @Column(name = "confirmed_revenue")
  private BigDecimal confirmedRevenue = ZERO;

  @Column(name = "canceled_revenue")
  private BigDecimal canceledRevenue = ZERO;

  @Column(name = "not_confirmed_fee")
  private BigDecimal notConfirmedFee = ZERO;

  @Column(name = "confirmed_fee")
  private BigDecimal confirmedFee = ZERO;

  @Column(name = "canceled_fee")
  private BigDecimal canceledFee = ZERO;

  @Column(name = "referer")
  @Nullable
  private String referer;

  @Column(name = "keywords")
  @Nullable
  private String keywords;

  @Column(name = "action_count")
  private int actionCount;

  @Column(name = "confirmed_action_count")
  private int confirmedActionCount;

  @Column(name = "canceled_action_count")
  private int canceledActionCount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(name = "cashback_target_id")
  private String cashbackTargetId;

  @Column(name = "cashback_referrer")
  private String cashbackReferrer;

  @Column(name = "site_id")
  private Long siteId;

  @Override
  public Long id() {
    return id;
  }

  public OfferStat() {
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

  public OfferStat setBannerId(Long bannerId) {
    this.bannerId = bannerId;
    return this;
  }

  public OfferStat setOfferId(Long offerId) {
    this.offerId = offerId;
    return this;
  }

  public OfferStat setAffiliateId(Long affiliateId) {
    this.affiliateId = affiliateId;
    return this;
  }

  public OfferStat setSourceId(String sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public OfferStat setReferer(String referer) {
    this.referer = referer;
    return this;
  }

  public OfferStat setKeywords(String keywords) {
    this.keywords = keywords;
    return this;
  }

  public OfferStat setMaster(Long master) {
    this.master = master;
    return this;
  }

  public Long affiliateId() {
    return affiliateId;
  }

  public Long offerId() {
    return this.offerId;
  }

  public BaseOffer offer() {
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

  public BigDecimal notConfirmedRevenue() {
    return nullToZero(this.notConfirmedRevenue);
  }

  public BigDecimal canceledRevenue() {
    return nullToZero(this.canceledRevenue);
  }

  public BigDecimal confirmedRevenue() {
    return nullToZero(this.confirmedRevenue);
  }

  public BigDecimal notConfirmedFee() {
    return nullToZero(this.notConfirmedFee);
  }

  public BigDecimal canceledFee() {
    return nullToZero(this.canceledFee);
  }

  public BigDecimal confirmedFee() {
    return nullToZero(this.confirmedFee);
  }

  public Long master() {
    return master;
  }

  public OfferStat incClicks() {
    clickCount++;
    return this;
  }

  public OfferStat incShows() {
    showCount++;
    return this;
  }

  public OfferStat incLeads() {
    leadsCount++;
    return this;
  }

  public OfferStat incSales() {
    salesCount++;
    return this;
  }

  public OfferStat incActions() {
    actionCount++;
    return this;
  }

  public OfferStat incConfirmedActions() {
    confirmedActionCount++;
    return this;
  }

  public OfferStat incCanceledActions() {
    canceledActionCount++;
    return this;
  }

  public void addToConfirmedRevenue(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (confirmedRevenue == null)
      confirmedRevenue = ZERO;
    confirmedRevenue = confirmedRevenue.add(amount);
  }

  public void subtractFromConfirmedRevenue(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (confirmedRevenue == null)
      confirmedRevenue = ZERO;
    confirmedRevenue = confirmedRevenue.subtract(amount);
  }

  public OfferStat addToNotConfirmedRevenue(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (notConfirmedRevenue == null)
      notConfirmedRevenue = ZERO;
    notConfirmedRevenue = notConfirmedRevenue.add(amount);
    return this;
  }

  public OfferStat addToNotConfirmedFee(BigDecimal fee) {
    checkArgument(fee.signum() >= 0);
    notConfirmedFee = nullToZero(notConfirmedFee).add(fee);
    return this;
  }

  public void approveMoney(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (notConfirmedRevenue == null)
      notConfirmedRevenue = ZERO;
    if (confirmedRevenue == null)
      confirmedRevenue = ZERO;
    notConfirmedRevenue = notConfirmedRevenue.subtract(amount);
    confirmedRevenue = confirmedRevenue.add(amount);
  }

  public void cancelMoney(BigDecimal amount) {
    checkArgument(amount.signum() == 1);
    if (notConfirmedRevenue == null)
      notConfirmedRevenue = ZERO;
    if (canceledRevenue == null)
      canceledRevenue = ZERO;
    notConfirmedRevenue = notConfirmedRevenue.subtract(amount);
    canceledRevenue = canceledRevenue.add(amount);
  }

  public void cancelFee(BigDecimal fee) {
    checkArgument(fee.signum() == 1);
    if (notConfirmedFee == null)
      notConfirmedFee = ZERO;
    if (canceledFee == null)
      canceledFee = ZERO;
    notConfirmedFee = notConfirmedFee.subtract(fee);
    canceledFee = canceledFee.add(fee);
  }

  private BigDecimal nullToZero(BigDecimal number) {
    if (number == null)
      return ZERO;
    return number;
  }

  public void approveFee(BigDecimal fee) {
    checkArgument(fee.signum() == 1);
    notConfirmedFee = nullToZero(notConfirmedFee).subtract(fee);
    confirmedFee = nullToZero(confirmedFee).add(fee);
  }

  public OfferStat setSubs(Subs subs) {
    this.subId = subs.subId();
    this.subId1 = subs.subId1();
    this.subId2 = subs.subId2();
    this.subId3 = subs.subId3();
    this.subId4 = subs.subId4();
    return this;
  }

  public OfferStat setProduct(Product product) {
    this.product = product;
    return this;
  }

  public OfferStat setCashbackTargetId(String cashbackTargetId) {
    this.cashbackTargetId = cashbackTargetId;
    return this;
  }

  public String cashbackTargetId() {
    return cashbackTargetId;
  }

  public OfferStat setCashbackReferrer(String cashbackReferrer) {
    this.cashbackReferrer = cashbackReferrer;
    return this;
  }

  public String cashbackReferrer() {
    return this.cashbackReferrer;
  }

  public Long siteId() {
    return siteId;
  }

  public OfferStat setSiteId(Long siteId) {
    this.siteId = siteId;
    return this;
  }
}
