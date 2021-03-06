package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@XmlRootElement(name = "user")
public class XmlUser {
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "email")
  public String email;

  @XmlElement(name = "password-hash")
  public String passwordHash;
  
  @XmlElement(name = "first-name")
  public String firstName;
  
  @XmlElement(name = "last-name")
  public String lastName;
  
  @XmlElement(name = "organization")
  public String organization;
  
  @XmlElement(name = "phone")
  public String phone;
  
  @XmlElement(name = "source-url")
  public String sourceUrl;
  
  @XmlElement(name = "messenger-type")
  public String messengerType;
  
  @XmlElement(name = "messenger-uid")
  public String messengerUid;
  
  @XmlElement(name = "wmr")
  public String wmr;

  @XmlElement(name = "secret-key")
  public String secretKey;

  @XmlElement(name = "customer-account")
  public XmlAccount customerAccount;

  @XmlElement(name = "revenue")
  public String revenue;

  @XmlElement(name = "developer-account")
  public XmlAccount developerAccount;
  
  @XmlElement(name = "developer-account-not-confirmed")
  public XmlAccount developerAccountNotConfirmed;
  
  @XmlElement(name = "advertiser-account")
  public XmlAccount advertiserAccount;

  @XmlElement(name = "affiliate-account")
  public XmlAccount affiliateAccount;

  @XmlElement(name = "affiliate-account-not-confirmed")
  public XmlAccount affiliateAccountNotConfirmed;

  @XmlElementWrapper(name = "roles")
  @XmlElement(name = "role")
  public Set<String> roles;
  
  @XmlElement(name = "referrer")
  public Long referrer;

  @XmlElementWrapper(name = "referrals")
  @XmlElement(name = "referral")
  public Set<String> referrals = newHashSet();
  
  @XmlElement(name = "confirmed")
  public Boolean confirmed;
  
  @XmlElement(name = "blocked")
  public Boolean blocked;
  
  @XmlElement(name = "block-reason")
  public String blockReason;
  
  @XmlElement(name = "register-time")
  public String registerTime;

}
