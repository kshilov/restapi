package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "answer")
public class XmlAnswer {
  @XmlElement(name = "question-id")
  public Integer questionId;
  @XmlElement(name = "profile-id")
  public String profileId;
  @XmlAttribute
  public boolean vote;
  public String text;
  public Integer choice;
}
