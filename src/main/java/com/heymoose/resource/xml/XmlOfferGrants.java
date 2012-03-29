package com.heymoose.resource.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement(name = "grants")
public class XmlOfferGrants {
  
  @XmlAttribute(name = "count")
  public Long count;
  
  @XmlElement(name = "grant")
  public List<XmlOfferGrant> grants = Lists.newArrayList();
}
