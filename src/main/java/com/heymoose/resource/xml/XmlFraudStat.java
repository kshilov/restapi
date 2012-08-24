package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "fraud-stats")
public final class XmlFraudStat {

  @XmlRootElement(name = "stat")
  private static class XmlStat {

    @XmlElement
    public XmlAffiliateBasic affiliate;

    @XmlElement
    public Long canceled;

    @XmlElement
    public Long approved;

    @XmlElement(name = "not-confirmed")
    public Long notConfirmed;

    @XmlElement
    public BigDecimal rate;

    public XmlStat(Map<String, Object> map) {
      this.affiliate = new XmlAffiliateBasic(map);
      this.approved = SqlLoader.extractLong(map.get("approved"));
      this.canceled = SqlLoader.extractLong(map.get("canceled"));
      this.notConfirmed = SqlLoader.extractLong(map.get("not_confirmed"));
      this.rate = SqlLoader.scaledDecimal(map.get("rate"));
    }

    protected XmlStat() { }
  }


  @XmlAttribute(name = "count")
  public Long count;

  @XmlElement(name = "stat")
  public List<XmlStat> statList = Lists.newArrayList();

  protected XmlFraudStat() { }

  public XmlFraudStat(Pair<QueryResult, Long> pair) {
    this.count = pair.snd;
    for (Map<String, Object> record : pair.fst) {
      this.statList.add(new XmlStat(record));
    }
  }
}
