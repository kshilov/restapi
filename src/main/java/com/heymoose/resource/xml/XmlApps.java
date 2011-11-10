package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement(name = "apps")
public class XmlApps {
  @XmlElement(name = "app")
  public List<XmlApp> apps = newArrayList();
}
