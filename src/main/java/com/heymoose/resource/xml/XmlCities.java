package com.heymoose.resource.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cities")
public class XmlCities {
  @XmlElement(name = "city")
  public List<XmlCity> cities;
}
