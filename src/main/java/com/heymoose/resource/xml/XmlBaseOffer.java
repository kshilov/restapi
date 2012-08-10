package com.heymoose.resource.xml;

import com.google.common.collect.Sets;
import com.heymoose.domain.offer.BaseOffer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Set;

@XmlRootElement(name = "offer")
public final class XmlBaseOffer {

  protected XmlBaseOffer() { }


  public XmlBaseOffer(BaseOffer offer) {
    this.id = offer.id();
    this.payMethod = offer.payMethod().toString();
    if (offer.cpaPolicy() != null)
      this.cpaPolicy = offer.cpaPolicy().toString();
    this.cost = offer.cost();
    this.cost2 = offer.cost2();
    this.percent = offer.percent();
    this.active = offer.active();
    this.creationTime = offer.creationTime().toString();
    this.title = offer.title();
    this.autoApprove = offer.autoApprove();
    this.reentrant = offer.reentrant();
    this.code = offer.code();
    this.holdDays = offer.holdDays();
    this.exclusive = offer.exclusive();

    for (String region : offer.regions())
      this.regions.add(region);
  }

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "advertiser")
  public XmlUser advertiser;

  @XmlElement(name = "account")
  public XmlAccount account;

  @XmlElement(name = "grant")
  public XmlOfferGrant grant;

  @XmlElement(name = "pay-method")
  public String payMethod;

  @XmlElement(name = "cpa-policy")
  public String cpaPolicy;

  @XmlElement(name = "name")
  public String name;

  @XmlElement(name = "description")
  public String description;

  @XmlElement(name = "cost")
  public BigDecimal cost;

  @XmlElement(name = "cost2")
  public BigDecimal cost2;

  @XmlElement(name = "percent")
  public BigDecimal percent;

  @XmlElement(name = "approved")
  public Boolean approved;

  @XmlElement(name = "active")
  public Boolean active;

  @XmlElement(name = "creation-time")
  public String creationTime;

  @XmlElement(name = "title")
  public String title;

  @XmlElement(name = "url")
  public String url;

  @XmlElement(name = "auto-approve")
  public Boolean autoApprove;

  @XmlElement(name = "reentrant")
  public Boolean reentrant;

  @XmlElement(name = "code")
  public String code;

  @XmlElement(name = "hold-days")
  public Integer holdDays;

  @XmlElement
  public boolean exclusive;

  @XmlElementWrapper(name = "regions")
  @XmlElement(name = "region")
  public Set<String> regions = Sets.newHashSet();
}
