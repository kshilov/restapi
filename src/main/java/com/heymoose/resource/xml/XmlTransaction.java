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
  
  @XmlElement(name = "type")
  String type;
  
  @XmlElement(name = "creation-time")
  String creationTime;
  
  @XmlElement(name = "end-time")
  String endTime;
}
