package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class XmlQuestions {
  @XmlElement(name = "questions")
  public List<XmlQuestion> questions;
}
