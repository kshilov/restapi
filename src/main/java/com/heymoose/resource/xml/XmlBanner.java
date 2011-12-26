package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "banner")
public class XmlBanner {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "image")
  public String imageBase64;

  @XmlElement(name = "banner-size")
  public XmlBannerSize bannerSize;
}
