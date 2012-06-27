package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Map;

import static com.heymoose.domain.affiliate.OfferStats.*;

@XmlRootElement(name = "stats")
public final class XmlTotalStats {

  @XmlRootElement(name = "stat")
  private static final class XmlDestination {

    @XmlElement
    public BigDecimal partner;
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

  public XmlTotalStats(Map<String, BigDecimal> map) {
    confirmed = new XmlDestination();
    confirmed.fee = map.get(CONFIRMED_FEE);
    confirmed.partner = map.get(CONFIRMED_PARTNER);
    confirmed.sum = map.get(CONFIRMED_SUM);

    notConfirmed = new XmlDestination();
    notConfirmed.fee = map.get(NOT_CONFIRMED_FEE);
    notConfirmed.partner = map.get(NOT_CONFIRMED_PARTNER);
    notConfirmed.sum = map.get(NOT_CONFIRMED_SUM);

    canceled = new XmlDestination();
    canceled.sum = map.get(CANCELED);
  }

  protected XmlTotalStats() { }
}
