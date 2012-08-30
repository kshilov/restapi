package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "category")
public class XmlCategory {

  @XmlRootElement(name = "grouping")
  public static class XmlGrouping {
    @XmlAttribute(name = "id")
    public Long id;

    @XmlValue
    public String name;
  }
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "grouping")
  public XmlGrouping grouping = new XmlGrouping();

  @XmlElement(name = "name")
  public String name;
}
