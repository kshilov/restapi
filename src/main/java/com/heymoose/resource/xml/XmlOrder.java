package com.heymoose.resource.xml;

import java.math.BigDecimal;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "order")
public class XmlOrder {
  @XmlAttribute
  public Long id;

  @XmlElement(name = "user-id")
  public Long userId;
  
  @XmlElement(name = "user")
  public XmlUser user;

  @XmlElement(name = "disabled")
  public Boolean disabled;

  @XmlElement(name = "paused")
  public Boolean paused;
  
  @XmlElement(name = "cpa")
  public BigDecimal cpa;
  
  @XmlElement(name = "creation-time")
  public String creationTime;
  
  @XmlElement(name = "account")
  public XmlAccount account;
  
  // Common offer fields
  
  @XmlElement(name = "offer-id")
  public Long offerId;
  
  @XmlElement(name = "title")
  public String title;
  
  @XmlElement(name = "url")
  public String url;
  
  @XmlElement(name = "auto-approve")
  public Boolean autoApprove;
  
  @XmlElement(name = "reentrant")
  public Boolean reentrant;
  
  @XmlElement(name = "type")
  public String type;
  
  // Regular offer fields

  @XmlElement(name = "description")
  public String description;
  
  // Video offer fields
  
  @XmlElement(name = "video-url")
  public String videoUrl;

  @XmlElement(name = "image")
  public String imageBase64;
  
  // Banner offer fields

  @XmlElement(name = "banner-size")
  public XmlBannerSize bannerSize;

  @XmlElementWrapper(name = "banners")
  @XmlElement(name = "banner")
  public List<XmlBanner> banners;

  // Targeting fields
  
  @XmlElement(name = "male")
  public Boolean male;
  
  @XmlElement(name = "min-age")
  public Integer minAge;
  
  @XmlElement(name = "max-age")
  public Integer maxAge;
  
  @XmlElement(name = "min-hour")
  public Integer minHour;
  
  @XmlElement(name = "max-hour")
  public Integer maxHour;
  
  @XmlElement(name = "city-filter-type")
  public String cityFilterType;
  
  @XmlElement(name = "cities")
  public XmlCities cities;
  
  @XmlElement(name = "app-filter-type")
  public String appFilterType;
  
  @XmlElement(name = "apps")
  public XmlApps apps;
}
