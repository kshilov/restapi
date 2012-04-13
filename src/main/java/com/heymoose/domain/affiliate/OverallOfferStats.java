package com.heymoose.domain.affiliate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stat")
public class OverallOfferStats {

  @XmlElement(name = "offer-id")
  public long offerId;

  @XmlElement(name = "name")
  public String name;

  @XmlElement(name = "shows")
  public long shows;

  @XmlElement(name = "clicks")
  public long clicks;

  @XmlElement(name = "leads")
  public long leads;

  @XmlElement(name = "sales")
  public long sales;

  @XmlElement(name = "confirmed-revenue")
  public double confirmedRevenue;

  @XmlElement(name = "not-confirmed-revenue")
  public double notConfirmedRevenue;

  @XmlElement(name = "canceled-revenue")
  public double canceledRevenue;

  @XmlElement(name = "ctr")
  public Double ctr;

  @XmlElement(name = "cr")
  public Double cr;

  @XmlElement(name = "ecpc")
  public Double ecpc;

  @XmlElement(name = "ecpm")
  public Double ecpm;

  protected OverallOfferStats() {}

  public OverallOfferStats(long offerId, String name, long shows, long clicks, long leads, long sales,
                           double confirmedRevenue, double notConfirmedRevenue, double canceledRevenue,
                           Double ctr, Double cr, Double ecpc, Double ecpm) {
    this.offerId = offerId;
    this.name = name;
    this.shows = shows;
    this.clicks = clicks;
    this.leads = leads;
    this.sales = sales;
    this.confirmedRevenue = confirmedRevenue;
    this.notConfirmedRevenue = notConfirmedRevenue;
    this.canceledRevenue = canceledRevenue;
    this.ctr = ctr;
    this.cr = cr;
    this.ecpc = ecpc;
    this.ecpm = ecpm;
  }

  @Override
  public String toString() {
    return "OverallOfferStats{" +
        "offerId=" + offerId +
        ", shows=" + shows +
        ", clicks=" + clicks +
        ", leads=" + leads +
        ", sales=" + sales +
        ", confirmedRevenue=" + confirmedRevenue +
        ", notConfirmedRevenue=" + notConfirmedRevenue +
        ", canceledRevenue=" + canceledRevenue +
        ", ctr=" + ctr +
        ", cr=" + cr +
        ", ecpc=" + ecpc +
        ", ecpm=" + ecpm +
        '}';
  }
}
