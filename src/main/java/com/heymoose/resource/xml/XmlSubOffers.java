package com.heymoose.resource.xml;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "suboffers")
public class XmlSubOffers {
  
  @XmlAttribute(name = "count")
  public Long count;
  
  @XmlElement(name = "suboffer")
  public List<XmlSubOffer> suboffers = Lists.newArrayList();

  public void add(XmlSubOffer sub) {
    suboffers.add(sub);
  }
}
