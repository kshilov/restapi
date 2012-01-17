package com.heymoose.resource.xml;

import static com.google.common.collect.Sets.newHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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
  public XmlAccount customerAccount;

  @XmlElement(name = "revenue")
  public String revenue;

  @XmlElement(name = "customer-secret")
  public String customerSecret;

  @XmlElement(name = "developer-account")
  public XmlAccount developerAccount;

  @XmlElementWrapper(name = "apps")
  @XmlElement(name = "app")
  public Set<XmlApp> apps;
  
  @XmlElementWrapper(name = "orders")
  @XmlElement(name = "order")
  public Set<XmlOrder> orders;

  @XmlElementWrapper(name = "roles")
  @XmlElement(name = "role")
  public Set<String> roles;
  
  @XmlElement(name = "referrer")
  public Long referrer;

  @XmlElementWrapper(name = "referrals")
  @XmlElement(name = "referral")
  public Set<String> referrals = newHashSet();
}
