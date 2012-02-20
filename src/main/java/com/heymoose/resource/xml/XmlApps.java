package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "apps")
public class XmlApps {
  @XmlElement(name = "app")
  public List<XmlApp> apps = newArrayList();
}
