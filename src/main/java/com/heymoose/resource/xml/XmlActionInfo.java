package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "action")
public class XmlActionInfo {
  @XmlElement(name = "token")
  public String token;

  @XmlElement(name = "offer-code")
  public String offerCode;

  @XmlElement(name = "transaction-id")
  public String transactionId;

  @XmlElement(name = "price")
  public Double price;

}
