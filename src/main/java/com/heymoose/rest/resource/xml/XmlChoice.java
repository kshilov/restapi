package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class XmlChoice {
  @XmlAttribute
  public Integer id;
  public String text;
}
