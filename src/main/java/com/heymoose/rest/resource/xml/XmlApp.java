package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "app")
public class XmlApp {

  @XmlAttribute(name = "id")
  public Integer appId;

  @XmlAttribute(name = "secret")
  public String secret;
}
