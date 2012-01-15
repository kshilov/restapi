package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stat")
public class XmlStat {

  @XmlAttribute(name = "time")
  public Long time;
  
  @XmlAttribute(name = "shows")
  public Integer shows;

  @XmlAttribute(name = "actions")
  public Integer actions;

  @XmlAttribute(name = "ctr")
  public Double ctr;
}
