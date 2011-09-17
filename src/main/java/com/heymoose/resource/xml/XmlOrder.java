package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "order")
public class XmlOrder {
  @XmlAttribute
  public Long id;

  @XmlElement(name = "title")
  public String title;

  @XmlElement(name = "balance")
  public String balance;

  @XmlElement(name = "user-id")
  public Long userId;

  @XmlElement(name = "approved")
  public Boolean approved;

  @XmlElement(name = "deleted")
  public Boolean deleted;
}
