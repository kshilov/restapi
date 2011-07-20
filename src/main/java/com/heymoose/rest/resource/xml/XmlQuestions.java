package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "questions")
public class XmlQuestions {
  @XmlElement(name = "question")
  public List<XmlQuestion> questions;
}
