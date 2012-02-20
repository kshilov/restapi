package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orders")
public class XmlOrders {
  @XmlElement(name = "order")
  public List<XmlOrder> orders = Lists.newArrayList();
}
