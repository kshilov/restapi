package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "performer")
public class XmlPerformer {
  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "ext-id")
  public String extId;
  
  @XmlElement(name = "platform")
  public String platform;
  
  @XmlElement(name = "creation-time")
  public String creationTime;
  
  @XmlElement(name = "inviter")
  public XmlPerformer inviter;
  
  @XmlElement(name = "male")
  public Boolean male;
  
  @XmlElement(name = "year")
  public Integer year;
}
