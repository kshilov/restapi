package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "settings")
public class XmlSettings {
  
  @XmlElement(name = "m")
  public String M;
  
  @XmlElement(name = "q")
  public String Q;
  
  @XmlElement(name = "d-avg")
  public String Davg;
}
