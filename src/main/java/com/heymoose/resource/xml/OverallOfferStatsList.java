package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.affiliate.OverallOfferStats;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stats")
public class OverallOfferStatsList {
  @XmlElement(name = "stat")
  public List<OverallOfferStats> stats = newArrayList();
}
