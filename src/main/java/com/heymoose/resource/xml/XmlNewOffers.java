package com.heymoose.resource.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement(name = "offers")
public class XmlNewOffers {
  
  @XmlAttribute(name = "count")
  public Long count;
  
  @XmlElement(name = "offer")
  public List<XmlNewOffer> offers = Lists.newArrayList();
}
