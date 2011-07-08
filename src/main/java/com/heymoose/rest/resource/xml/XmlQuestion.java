package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlQuestion {
  @XmlElement(name = "order-id")
  public int orderId;
  public String text;
}
