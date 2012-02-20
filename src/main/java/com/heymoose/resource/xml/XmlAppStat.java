package com.heymoose.resource.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "app-stat")
public class XmlAppStat {

  @XmlAttribute(name = "id")
  public Long id;
  
  @XmlElement(name = "shows-overall")
  public Long showsOverall;
  
  @XmlElement(name = "actions-overall")
  public Long actionsOverall;
  
  @XmlElement(name = "dau-average")
  public Double dauAverage;
  
  @XmlElement(name = "dau-day0")
  public Double dauDay0;
  
  @XmlElement(name = "dau-day1")
  public Double dauDay1;
  
  @XmlElement(name = "dau-day2")
  public Double dauDay2;
  
  @XmlElement(name = "dau-day3")
  public Double dauDay3;
  
  @XmlElement(name = "dau-day4")
  public Double dauDay4;
  
  @XmlElement(name = "dau-day5")
  public Double dauDay5;
  
  @XmlElement(name = "dau-day6")
  public Double dauDay6;
}
