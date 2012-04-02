package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "category")
public class XmlCategory {
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "grouping")
  public String grouping;

  @XmlElement(name = "name")
  public String name;
}
