package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "app")
public class XmlApp {
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "secret")
  public String secret;
}
