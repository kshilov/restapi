package com.heymoose.resource.xml;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.heymoose.infrastructure.service.OfferStats.*;

@XmlRootElement(name = "stats")
public final class XmlTotalStats {

  @XmlEnum(String.class)
  private enum XmlStatGroupType {
    INCOME, OUTGO, PROFIT, CANCELED, HOLD, DIFFERENCE
  }

  @XmlRootElement
  private static final class XmlStatGroup {

    @XmlAttribute
    public XmlStatGroupType type;

    @XmlAttribute
    public BigDecimal sum = BigDecimal.ZERO;

    @XmlElement(name = "entry")
    public List<XmlStatEntry> entryList = Lists.newArrayList();

    public XmlStatGroup() { }

    public XmlStatGroup(XmlStatGroupType type) {
      this.type = type;
    }

    public XmlStatGroup addEntry(String name, BigDecimal value) {
      this.entryList.add(new XmlStatEntry(name, value));
      return addToSum(value);
    }

    public XmlStatGroup addToSum(BigDecimal value) {
      this.sum = sum.add(value);
      return this;
    }

  }

  @XmlRootElement
  private static final class XmlStatEntry {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public BigDecimal value;

    public XmlStatEntry() { }

    public XmlStatEntry(String name, BigDecimal value) {
      this.name = name;
      this.value = value;
    }

  }

  @XmlElement(name = "group")
  public List<XmlStatGroup> groupList = Lists.newArrayList();

  public XmlTotalStats(Map<String, BigDecimal> map) {
    BigDecimal incomeValue = map.get(CONFIRMED_SUM).add(map.get(EXPIRED_SUM));
    XmlStatGroup income = new XmlStatGroup(XmlStatGroupType.INCOME)
        .addEntry("Выплаты рекламодателей", incomeValue);
    XmlStatGroup outgo = new XmlStatGroup(XmlStatGroupType.OUTGO)
        .addEntry("Подтверждённые средства партнёра",
            map.get(CONFIRMED_AFFILIATE))
        .addEntry("Неподтверждённые средства партнёра вне холда",
            map.get(EXPIRED_AFFILIATE))
        .addEntry("MLM", map.get(MLM));
    XmlStatGroup profit = new XmlStatGroup(XmlStatGroupType.PROFIT)
        .addEntry("Подтверждённая комиссия", map.get(CONFIRMED_FEE))
        .addEntry("Неподтверждённая комиссия вне холда", map.get(EXPIRED_FEE));
    XmlStatGroup canceled = new XmlStatGroup(XmlStatGroupType.CANCELED)
        .addToSum(map.get(CANCELED_SUM));
    XmlStatGroup hold = new XmlStatGroup(XmlStatGroupType.HOLD)
        .addToSum(map.get(NOT_CONFIRMED_SUM).subtract(map.get(EXPIRED_SUM)));
    XmlStatGroup difference = new XmlStatGroup(XmlStatGroupType.DIFFERENCE)
        .addToSum(income.sum.subtract(outgo.sum).subtract(profit.sum));
    this.groupList.add(income);
    this.groupList.add(outgo);
    this.groupList.add(profit);
    this.groupList.add(difference);
    this.groupList.add(canceled);
    this.groupList.add(hold);
  }

  protected XmlTotalStats() { }
}
