package com.heymoose.resource.xml;

import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.site.Site;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "site")
public final class XmlSite {

  @XmlAttribute
  public Long id;

  @XmlElement
  public XmlUser affiliate;

  @XmlElement(name = "admin-state")
  public AdminState adminState;

  @XmlElement(name = "admin-comment")
  public String adminComment;

  @XmlElement
  public String name;

  @XmlElement
  public Site.Type type;
}
