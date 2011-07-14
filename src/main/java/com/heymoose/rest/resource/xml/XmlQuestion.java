package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "question")
public class XmlQuestion {
  @XmlAttribute
  public boolean poll;
  @XmlElement(name = "order-id")
  public int orderId;
  public String text;
  @XmlElement(name = "choices")
  public List<String> choices;
}
