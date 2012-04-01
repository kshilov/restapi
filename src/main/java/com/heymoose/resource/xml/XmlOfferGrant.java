package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offer")
public class XmlOfferGrant {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "offer")
  public XmlNewOffer offer;
  
  @XmlElement(name = "affiliate")
  public XmlUser affiliate;
  
  @XmlElement(name = "message")
  public String message;
  
  @XmlElement(name = "back-url")
  public String backUrl;
  
  @XmlElement(name = "postback-url")
  public String postbackUrl;

  @XmlElement(name = "state")
  public String state;

  @XmlElement(name = "blocked")
  public Boolean blocked;

  @XmlElement(name = "reject-reason")
  public String rejectReason;

  @XmlElement(name = "block-reason")
  public String blockReason;
}
