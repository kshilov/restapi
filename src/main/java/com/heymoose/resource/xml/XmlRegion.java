package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "region")
public class XmlRegion {
  @XmlElement(name = "country-code")
  public String countryCode;

  @XmlElement(name = "country-name")
  public String countryName;
}
