package com.heymoose.resource.xml;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entry")
public class XmlAccountingEntry {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "amount")
  public BigDecimal amount;
  
  @XmlElement(name = "descr")
  public String descr;
  
  @XmlElement(name = "event")
  public String event;
}
