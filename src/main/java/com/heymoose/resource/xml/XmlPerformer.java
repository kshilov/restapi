package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "performer")
public class XmlPerformer {
  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlAttribute(name = "ext-id")
  public String extId;
  
  @XmlAttribute(name = "creation-time")
  public String creationTime;
  
  @XmlAttribute(name = "inviter")
  public XmlPerformer inviter;
  
  @XmlAttribute(name = "male")
  public Boolean male;
  
  @XmlAttribute(name = "age")
  public Integer age;
}
