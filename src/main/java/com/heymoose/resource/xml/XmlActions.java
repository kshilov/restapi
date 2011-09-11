package com.heymoose.resource.xml;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "actions")
public class XmlActions {
  @XmlElement(name = "action")
  public List<XmlAction> actions = Lists.newArrayList();
}
