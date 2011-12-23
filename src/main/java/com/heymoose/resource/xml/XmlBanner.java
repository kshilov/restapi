package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "banner")
public class XmlBanner {

  @XmlElement(name = "image")
  public String imageBase64;


  @XmlElement(name = "banner-size")
  public XmlBannerSize bannerSize;
}
