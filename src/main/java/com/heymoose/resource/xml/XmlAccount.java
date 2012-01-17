package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "account")
public class XmlAccount {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "balance")
  public Double balance;
}
