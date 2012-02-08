package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "withdraw")
public class XmlWithdraw {

  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "amount")
  public Double amount;

  @XmlElement(name = "timestamp")
  public String timestamp;

  @XmlElement(name = "done")
  public Boolean done;
}
