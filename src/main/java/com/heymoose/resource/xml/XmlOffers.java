package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offers")
public class XmlOffers {
  @XmlElement(name = "offer")
  public List<XmlOffer> offers = newArrayList();
}
