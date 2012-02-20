package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "shows")
public class XmlOfferShows {
  @XmlElement(name = "show")
  public List<XmlOfferShow> shows = Lists.newArrayList();
}
