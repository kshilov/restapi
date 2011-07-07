package com.heymoose.rest.test;

import com.heymoose.rest.resource.xml.XmlApp;
import com.heymoose.rest.test.base.ApiTest;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class AppTest extends ApiTest {

  private final static Logger log = LoggerFactory.getLogger(AppTest.class);

  void putApp(XmlApp xmlApp) {
    client().path("app").path(xmlApp.appId.toString()).put(xmlApp);
  }

  XmlApp getApp(int id) {
    return client().path("app").path(Integer.toString(id)).get(XmlApp.class);
  }

  private XmlApp someXmlApp() {
    XmlApp xmlApp = new XmlApp();
    xmlApp.appId = 1;
    xmlApp.secret = "s";
    return xmlApp;
  }

  @Test public void put() {
    XmlApp xmlApp = someXmlApp();
    putApp(xmlApp);
    XmlApp saved = getApp(xmlApp.appId);
    assertEquals(xmlApp.appId, saved.appId);
    assertEquals(xmlApp.secret, saved.secret);
  }

  @Test public void putTwice() {
    XmlApp xmlApp = someXmlApp();
    putApp(xmlApp);
    putApp(xmlApp);
    XmlApp saved = getApp(xmlApp.appId);
    assertEquals(xmlApp.appId, saved.appId);
    assertEquals(xmlApp.secret, saved.secret);
  }

  @Test public void refreshSecret() {
    XmlApp xmlApp = someXmlApp();
    putApp(xmlApp);
    String secret = "new secret";
    xmlApp.secret = secret;
    putApp(xmlApp);
    XmlApp saved = getApp(xmlApp.appId);
    assertEquals(secret, saved.secret);
  }

  @Test public void get() {
    XmlApp xmlApp = someXmlApp();
    putApp(xmlApp);
    XmlApp app = getApp(xmlApp.appId);
    assertEquals(xmlApp.appId, app.appId);
  }

  @Test public void getNonExistent() {
    try {
      getApp(1);
      fail();
    } catch (UniformInterfaceException expected) {
      assertEquals(404, expected.getResponse().getStatus());
    }
  }
}
