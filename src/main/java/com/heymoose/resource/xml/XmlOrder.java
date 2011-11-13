package com.heymoose.resource.xml;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "order")
public class XmlOrder {
  @XmlAttribute
  public Long id;

  @XmlElement(name = "user-id")
  public Long userId;
  
  @XmlElement(name = "user")
  public XmlUser user;

  @XmlElement(name = "disabled")
  public Boolean disabled;
  
  @XmlElement(name = "cpa")
  public BigDecimal cpa;
  
  @XmlElement(name = "creation-time")
  public String creationTime;
  
  // Account fields
  
  @XmlElement(name = "balance")
  public String balance;
  
  @XmlElement(name = "allow-negative-balance")
  public Boolean allowNegativeBalance;
  
  // Offer fields
  
  @XmlElement(name = "title")
  public String title;
  
  @XmlElement(name = "description")
  public String description;
  
  @XmlElement(name = "body")
  public String body;
  
  @XmlElement(name = "auto-approve")
  public Boolean autoApprove;
  
  // Targeting fields
  
  @XmlElement(name = "male")
  public Boolean male;
  
  @XmlElement(name = "min-age")
  public Integer minAge;
  
  @XmlElement(name = "max-age")
  public Integer maxAge;
}
