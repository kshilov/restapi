package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "show")
public class XmlOfferShow {
  @XmlAttribute
  public Long id;
  
  @XmlValue
  public String showTime;
}
