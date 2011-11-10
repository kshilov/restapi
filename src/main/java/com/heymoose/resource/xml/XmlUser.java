package com.heymoose.resource.xml;

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

  @XmlElement(name = "password-hash")
  public String passwordHash;

  @XmlElement(name = "customer-account")
  public String customerAccount;

  @XmlElement(name = "customer-secret")
  public String customerSecret;

  @XmlElement(name = "developer-account")
  public String developerAccount;

  @XmlElement(name = "app")
  public XmlApp app;
  
  @XmlElementWrapper(name = "orders")
  @XmlElement(name = "order")
  public Set<XmlOrder> orders;

  @XmlElementWrapper(name = "roles")
  @XmlElement(name = "role")
  public Set<String> roles;
}
