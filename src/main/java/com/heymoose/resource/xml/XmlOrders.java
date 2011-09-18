package com.heymoose.resource.xml;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "orders")
public class XmlOrders {
  @XmlElement(name = "order")
  public List<XmlOrder> orders = Lists.newArrayList();
}
