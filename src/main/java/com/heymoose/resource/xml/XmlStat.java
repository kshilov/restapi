package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stat")
public class XmlStat {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlAttribute(name = "gender")
  public Boolean gender;
  
  @XmlAttribute(name = "year")
  public Integer year;
  
  @XmlAttribute(name = "city")
  public String city;
  
  @XmlAttribute(name = "time")
  public Long time;
  
  @XmlAttribute(name = "shows")
  public Integer shows;

  @XmlAttribute(name = "actions")
  public Integer actions;
  
  @XmlAttribute(name = "performers")
  public Integer performers;

  @XmlAttribute(name = "ctr")
  public Double ctr;
}
