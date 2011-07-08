package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "order")
public class XmlOrder {
  @XmlAttribute
  public int id;
  public String balance;
  public String name;
  public XmlTargeting targeting;
}
