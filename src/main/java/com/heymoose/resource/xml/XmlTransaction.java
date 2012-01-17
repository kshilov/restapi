package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transaction")
public class XmlTransaction {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "balance")
  Double balance;

  @XmlElement(name = "diff")
  Double diff;

  @XmlElement(name = "description")
  String description;
}
