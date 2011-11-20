package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement(name = "performers")
public class XmlPerformers {
  @XmlElement(name = "performer")
  public List<XmlPerformer> performers = newArrayList();
}
