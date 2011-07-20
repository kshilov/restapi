package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "question")
public class XmlQuestion {
  @XmlAttribute
  public boolean poll;
  @XmlElement(name = "order-id")
  public int orderId;
  public String text;
  @XmlElementWrapper(name = "choices")
  @XmlElement(name = "choice")
  public List<XmlChoice> choices;
}
