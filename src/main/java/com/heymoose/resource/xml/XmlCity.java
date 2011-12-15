package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "city")
public class XmlCity {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "name")
  public String name;
}
