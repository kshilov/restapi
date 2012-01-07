package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "banner-size")
public class XmlBannerSize {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "width")
  public Integer width;

  @XmlElement(name = "height")
  public Integer height;
  
  @XmlElement(name = "disabled")
  public Boolean disabled;
}
