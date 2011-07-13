package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "profiles")
public class XmlProfiles {
  @XmlElement(name = "profile")
  public List<XmlProfile> profiles;
}
