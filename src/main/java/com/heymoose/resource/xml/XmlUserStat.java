package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user-stat")
public class XmlUserStat {
  
  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "payments")
  public Double payments;
  
  @XmlElement(name = "unpaid-actions")
  public Long unpaidActions;
}
