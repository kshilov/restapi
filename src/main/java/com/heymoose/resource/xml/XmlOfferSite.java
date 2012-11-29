package com.heymoose.resource.xml;

import com.heymoose.domain.base.AdminState;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "placement")
public final class XmlOfferSite {

  @XmlAttribute
  public Long id;

  @XmlElement
  public XmlSite site;

  @XmlElement(name = "admin-state")
  public AdminState adminState;

  @XmlElement(name = "admin-comment")
  public String adminComment;

}
