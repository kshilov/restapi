package com.heymoose.domain.affiliate;

import com.google.common.base.Function;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stat")
public class OverallOfferStats {

    @XmlElement(name = "id")
    public long id;

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

    @XmlElement(name = "source-id")
    public String sourceId;

    @XmlElement(name = "sub-id")
    public String subId;

    @XmlElement(name = "sub-id-1")
    public String subId1;

    @XmlElement(name = "sub-id-2")
    public String subId2;

    @XmlElement(name = "sub-id-3")
    public String subId3;

    @XmlElement(name = "sub-id-4")
    public String subId4;

    public static Function<OverallOfferStats, Long> ID = new Function<OverallOfferStats, Long>() {
        @Override
        public Long apply(@Nullable OverallOfferStats overallOfferStats) {
            return overallOfferStats.id;
        }
    };

    protected OverallOfferStats() {
    }

    public OverallOfferStats(long id, String name, long shows, long clicks, long leads, long sales,
                             double confirmedRevenue, double notConfirmedRevenue, double canceledRevenue,
                             Double ctr, Double cr, Double ecpc, Double ecpm, String sourceId,
                             String subId, String subId1, String subId2, String subId3, String subId4) {
        this.id = id;
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
        this.sourceId = sourceId;
        this.subId = subId;
        this.subId1 = subId1;
        this.subId2 = subId2;
        this.subId3 = subId3;
        this.subId4 = subId4;
    }

    @Override
    public String toString() {
        return "OverallOfferStats{" +
            "id=" + id +
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
