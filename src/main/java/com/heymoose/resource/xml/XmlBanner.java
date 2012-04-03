package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "banner")
public class XmlBanner {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "width")
  public Integer width;
  
  @XmlElement(name = "height")
  public Integer height;
  
  @XmlElement(name = "mime-type")
  public String mimeType;
}
