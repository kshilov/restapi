package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Map;

import static com.heymoose.infrastructure.OfferStats.*;

@XmlRootElement(name = "stats")
public final class XmlTotalStats {

  @XmlRootElement(name = "stat")
  private static final class XmlDestination {

    @XmlElement
    public BigDecimal affiliate;
    @XmlElement
    public BigDecimal fee;
    @XmlElement
    public BigDecimal sum;
  }

  @XmlElement
  public XmlDestination confirmed;
  @XmlElement(name = "not-confirmed")
  public XmlDestination notConfirmed;
  @XmlElement
  public XmlDestination canceled;
  @XmlElement
  public XmlDestination expired;

  public XmlTotalStats(Map<String, BigDecimal> map) {
    confirmed = new XmlDestination();
    confirmed.fee = map.get(CONFIRMED_FEE);
    confirmed.affiliate = map.get(CONFIRMED_AFFILIATE);
    confirmed.sum = map.get(CONFIRMED_SUM);

    notConfirmed = new XmlDestination();
    notConfirmed.fee = map.get(NOT_CONFIRMED_FEE);
    notConfirmed.affiliate = map.get(NOT_CONFIRMED_AFFILIATE);
    notConfirmed.sum = map.get(NOT_CONFIRMED_SUM);

    expired = new XmlDestination();
    expired.fee = map.get(EXPIRED_FEE);
    expired.affiliate = map.get(EXPIRED_AFFILIATE);
    expired.sum = map.get(EXPIRED_SUM);

    canceled = new XmlDestination();
    canceled.fee = map.get(CANCELED_FEE);
    canceled.affiliate = map.get(CANCELED_AFFILIATE);
    canceled.sum = map.get(CANCELED_SUM);
  }

  protected XmlTotalStats() { }
}
