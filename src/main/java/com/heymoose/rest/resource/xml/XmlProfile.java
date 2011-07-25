package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "profile'")
public class XmlProfile {
  @XmlAttribute(name = "id")
  public String profileId;
}

