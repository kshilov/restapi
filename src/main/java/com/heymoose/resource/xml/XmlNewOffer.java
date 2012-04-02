package com.heymoose.resource.xml;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

@XmlRootElement(name = "offer")
public class XmlNewOffer {

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
  
  @XmlElement(name = "logo-filename")
  public String logoFileName;
  
  @XmlElement(name = "cost")
  public BigDecimal cost;
  
  @XmlElement(name = "percent")
  public BigDecimal percent;
  
  @XmlElement(name = "approved")
  public Boolean approved;

  @XmlElement(name = "active")
  public Boolean active;
  
  @XmlElement(name = "block-reason")
  public String blockReason;
  
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
  
  @XmlElementWrapper(name = "regions")
  @XmlElement(name = "region")
  public Set<String> regions = Sets.newHashSet();
}
