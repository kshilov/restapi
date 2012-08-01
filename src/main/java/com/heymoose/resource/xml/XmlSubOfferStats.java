package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class XmlSubOfferStats {

  private static final class XmlSubOfferStat {
    @XmlElement(name = "id")
    public long id;

    @XmlElement(name = "name")
    public String name;

    @XmlElement(name = "exclusive")
    public boolean exclusive;

    @XmlElement(name = "leads")
    public long leads;

    @XmlElement(name = "sales")
    public long sales;

    @XmlElement(name = "confirmed-revenue")
    public BigDecimal confirmedRevenue;

    @XmlElement(name = "not-confirmed-revenue")
    public BigDecimal notConfirmedRevenue;

    @XmlElement(name = "canceled-revenue")
    public BigDecimal canceledRevenue;

    public XmlSubOfferStat(Map<String, Object> data) {
      this.id = SqlLoader.extractLong(data.get("id"));
      this.name = data.get("descr").toString();
      this.exclusive = SqlLoader.extractBoolean(data.get("exclusive"));
      this.leads = SqlLoader.extractLong(data.get("leads_count"));
      this.sales = SqlLoader.extractLong(data.get("sales_count"));
      this.canceledRevenue = SqlLoader.scaledDecimal(data.get("canceled_revenue"));
      this.confirmedRevenue = SqlLoader.scaledDecimal(
          data.get("confirmed_revenue"));
      this.notConfirmedRevenue = SqlLoader.scaledDecimal(data.get("not_confirmed_revenue"));
    }

  }

  @XmlElementWrapper(name = "stats")
  @XmlElement(name = "stat")
  public List<XmlSubOfferStat> statList = Lists.newArrayList();

  @XmlAttribute
  public Long count;

  public XmlSubOfferStats(Pair<QueryResult, Long> data) {
    for (Map<String, Object> map : data.fst) {
      statList.add(new XmlSubOfferStat(map));
    }
    this.count = data.snd;
  }
}
