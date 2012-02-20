package com.heymoose.resource.xml;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "app")
public class XmlApp {
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "title")
  public String title;

  @XmlElement(name = "secret")
  public String secret;

  @XmlElement(name = "url")
  public String url;

  @XmlElement(name = "callback")
  public String callback;

  @XmlElement(name = "user-id")
  public Long userId;
  
  @XmlElement(name = "user")
  public XmlUser user;
  
  @XmlElement(name = "platform")
  public String platform;

  @XmlElement(name = "deleted")
  public Boolean deleted;
  
  @XmlElement(name = "creation-time")
  public String creationTime;
  
  @XmlElement(name = "d")
  public BigDecimal d;
  
  @XmlElement(name = "t")
  public BigDecimal t;
  
  @XmlElement(name = "stats")
  public XmlAppStat stats;
}
