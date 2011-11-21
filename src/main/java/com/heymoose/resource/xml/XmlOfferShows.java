package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import com.google.common.collect.Lists;

@XmlRootElement(name = "shows")
public class XmlOfferShows {
  @XmlElement(name = "show")
  public List<XmlOfferShow> shows = Lists.newArrayList();
}
