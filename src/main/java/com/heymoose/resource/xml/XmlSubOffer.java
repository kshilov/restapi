package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement(name = "suboffer")
public class XmlSubOffer {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "pay-method")
  public String payMethod;
  
  @XmlElement(name = "cpa-policy")
  public String cpaPolicy;
  
  @XmlElement(name = "cost")
  public BigDecimal cost;
  
  @XmlElement(name = "cost2")
  public BigDecimal cost2;
  
  @XmlElement(name = "percent")
  public BigDecimal percent;

  @XmlElement(name = "affiliate-fee-type")
  public String affiliateFeeType;

  @XmlElement(name = "affiliate-fee")
  public BigDecimal affiliateFee;

  @XmlElement(name = "active")
  public Boolean active;
  
  @XmlElement(name = "creation-time")
  public String creationTime;
  
  @XmlElement(name = "title")
  public String title;
  
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
}
