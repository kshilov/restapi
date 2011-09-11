package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "action")
public class XmlAction {
  @XmlAttribute(name = "id")
  public Long id;

  @XmlElement(name = "performer-id")
  public Long performerId;

  @XmlElement(name = "offer-id")
  public Long offerId;

  @XmlElement(name = "done")
  public Boolean done;

  @XmlElement(name = "deleted")
  public Boolean deleted;

  @XmlElement(name = "creation-time")
  public String creationTime;
}
