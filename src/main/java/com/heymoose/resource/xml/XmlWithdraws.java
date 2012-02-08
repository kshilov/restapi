package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "withdraws")
public class XmlWithdraws {

  @XmlAttribute(name = "account-id")
  public Long accountId;

  @XmlElement(name = "withdraw")
  public List<XmlWithdraw> withdraws = newArrayList();
}