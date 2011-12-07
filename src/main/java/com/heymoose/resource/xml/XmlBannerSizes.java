package com.heymoose.resource.xml;

import static com.google.common.collect.Lists.newArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "banner-sizes")
public class XmlBannerSizes {
  @XmlElement(name = "banner-size")
  public List<XmlBannerSize> bannerSizes = newArrayList();
}
