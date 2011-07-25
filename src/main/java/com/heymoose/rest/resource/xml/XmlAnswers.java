package com.heymoose.rest.resource.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "answers")
public class XmlAnswers {
  @XmlElement(name = "answer")
  public List<XmlAnswer> answers;
}
