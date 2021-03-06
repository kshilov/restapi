package com.heymoose.resource.xml;

import com.google.common.collect.Sets;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Set;

@XmlRootElement(name = "offer")
public class XmlOffer {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "advertiser")
  public XmlUser advertiser;
  
  @XmlElement(name = "account")
  public XmlAccount account;
  
  @XmlElementWrapper(name = "suboffers")
  @XmlElement(name = "suboffer")
  public Set<XmlSubOffer> suboffers = Sets.newHashSet();

  @XmlElementWrapper(name = "categories")
  @XmlElement(name = "category")
  public Set<XmlCategory> categories = Sets.newHashSet();

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
  
  @XmlElement(name = "short-description")
  public String shortDescription;
  
  @XmlElement(name = "cr")
  public BigDecimal cr;
  
  @XmlElement(name = "showcase")
  public Boolean showcase;
  
  @XmlElement(name = "logo-filename")
  public String logoFileName;
  
  @XmlElement(name = "cost")
  public BigDecimal cost;

  @XmlElement(name = "cost2")
  public BigDecimal cost2;

  @XmlElement(name = "percent")
  public BigDecimal percent;

  @XmlElement(name = "fee-type")
  public String feeType;

  @XmlElement(name = "affiliate-cost")
  public BigDecimal affiliateCost;

  @XmlElement(name = "affiliate-cost2")
  public BigDecimal affiliateCost2;

  @XmlElement(name = "affiliate-percent")
  public BigDecimal affiliatePercent;

  @XmlElement(name = "approved")
  public Boolean approved;

  @XmlElement(name = "active")
  public Boolean active;
  
  @XmlElement(name = "block-reason")
  public String blockReason;
  
  @XmlElement(name = "creation-time")
  public String creationTime;
  
  @XmlElement(name = "launch-time")
  public String launchTime;
  
  @XmlElement(name = "allow-deeplink")
  public Boolean allowDeeplink;
  
  @XmlElement(name = "title")
  public String title;
  
  @XmlElement(name = "url")
  public String url;
  
  @XmlElement(name = "site-url")
  public String siteUrl;
  
  @XmlElement(name = "auto-approve")
  public Boolean autoApprove;
  
  @XmlElement(name = "reentrant")
  public Boolean reentrant;
  
  @XmlElement(name = "code")
  public String code;
  
  @XmlElement(name = "hold-days")
  public Integer holdDays;
  
  @XmlElement(name = "cookie-ttl")
  public Integer cookieTtl;
  
  @XmlElement(name = "token-param-name")
  public String tokenParamName;

  @XmlElement(name = "is-product-offer")
  public Boolean isProductOffer;

  @XmlElement
  public boolean exclusive;
  
  @XmlElementWrapper(name = "regions")
  @XmlElement(name = "region")
  public Set<String> regions = Sets.newHashSet();

  @XmlElement(name = "yml-url")
  public String ymlUrl;

  @XmlElement(name = "allow-cashback")
  public boolean allowCashback;

  @XmlElement(name = "placements-count")
  public Long placementCount;
}
