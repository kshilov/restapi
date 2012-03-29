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
  
  @XmlElement(name = "pay-method")
  public String payMethod;
  
  @XmlElement(name = "cpa-policy")
  public String cpaPolicy;
  
  @XmlElement(name = "name")
  public String name;
  
  @XmlElement(name = "cost")
  public BigDecimal cost;
  
  @XmlElement(name = "percent")
  public BigDecimal percent;
  
  @XmlElement(name = "disabled")
  public Boolean disabled;

  @XmlElement(name = "paused")
  public Boolean paused;
  
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
