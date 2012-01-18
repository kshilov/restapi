package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transactions")
public class XmlTransactions {

  @XmlAttribute(name = "count")
  public Integer count;

  @XmlElement(name = "transaction")
  public List<XmlTransaction> transactions = newArrayList();
}
