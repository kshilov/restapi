package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement(name = "stats")
public class XmlOverallOfferStatsList {
  @XmlAttribute(name = "count")
  public long count;

  @XmlElement(name = "stat")
  public List<XmlOverallOfferStats> stats = newArrayList();
}
