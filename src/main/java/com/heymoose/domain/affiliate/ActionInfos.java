package com.heymoose.domain.affiliate;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "actions")
public class ActionInfos {
  @XmlElement(name = "action")
  public List<ActionInfo> actions = newArrayList();
}
