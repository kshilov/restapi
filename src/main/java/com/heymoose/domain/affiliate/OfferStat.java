package com.heymoose.domain.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.Banner;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.base.BaseEntity;
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

  @Override
  public Long id() {
    return id;
  }

  protected OfferStat() {}

  public OfferStat(@Nullable Long bannerId, Long offerId, Long master, Long affId, @Nullable String subId,
                   @Nullable String sourceId) {
    this.bannerId = bannerId;
    this.offerId = offerId;
    this.master = master;
    this.affiliateId = affId;
    this.subId = subId;
    this.sourceId = sourceId;
  }

  public Offer offer() {
    return offer;
  }

  public User affiliate() {
    return affiliate;
  }

  public String sourceId() {
    return sourceId;
  }

  public String subId () {
    return subId;
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
