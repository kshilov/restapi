package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "order")
public class XmlOrder {
  @XmlAttribute
  public int id;
  public String balance;
  public String name;
  public XmlTargeting targeting;
  @XmlElementWrapper(name = "questions")
  @XmlElement(name = "question")
  public List<XmlQuestion> questions;
}
