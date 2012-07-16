package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stats")
public class OverallOfferStatsList {
  @XmlAttribute(name = "count")
  public long count;

  @XmlElement(name = "stat")
  public List<XmlOverallOfferStats> stats = newArrayList();
}
