package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "count")
public class XmlCount {
  @XmlValue
  public Long count;
}
