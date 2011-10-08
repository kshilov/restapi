package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offer")
public class XmlOffer {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "title")
  public String title;

  @XmlElement(name = "body")
  public String body;
}
