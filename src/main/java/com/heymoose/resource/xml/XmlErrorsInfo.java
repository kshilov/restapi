package com.heymoose.resource.xml;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "errors")
public final class XmlErrorsInfo {

  @XmlAttribute(name = "count")
  public Long count;

  @XmlElement(name = "error")
  public List<XmlErrorInfo> list = Lists.newArrayList();
}
