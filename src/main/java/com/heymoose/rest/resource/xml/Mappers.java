package com.heymoose.rest.resource.xml;

import com.heymoose.rest.domain.app.App;

public class Mappers {

  public static XmlApp toXmlApp(App app) {
    XmlApp ret = new XmlApp();
    ret.appId = app.id();
    ret.secret = app.secret();
    return ret;
  }
}
