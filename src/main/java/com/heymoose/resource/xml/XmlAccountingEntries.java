package com.heymoose.resource.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement(name = "entries")
public class XmlAccountingEntries {
  
  @XmlAttribute(name = "count")
  public Long count;
  
  @XmlElement(name = "entry")
  public List<XmlAccountingEntry> entries = Lists.newArrayList();
}
