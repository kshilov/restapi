package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offer-stat")
public class XmlOfferStat {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "shows-overall")
  public Long showsOverall;
  
  @XmlElement(name = "actions-overall")
  public Long actionsOverall;
}
