package com.heymoose.resource.xml;

import com.google.common.collect.Sets;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

@XmlRootElement(name = "user")
public class XmlUser {
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "email")
  public String email;

  @XmlElement(name = "nickname")
  public String nickname;

  @XmlElementWrapper(name = "orders")
  @XmlElement(name = "order")
  public Set<XmlOrder> orders = Sets.newHashSet();

  @XmlElement(name = "app")
  public XmlApp app;
}
