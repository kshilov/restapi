package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@XmlRootElement(name = "offers")
public class XmlOffers {
  @XmlElement(name = "offer")
  public List<XmlOffer> offers = newArrayList();
}
