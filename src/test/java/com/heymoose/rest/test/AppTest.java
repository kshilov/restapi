package com.heymoose.rest.test;

import com.heymoose.rest.resource.xml.XmlApp;
import com.heymoose.rest.test.base.RestTest;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppTest extends RestTest {

  void putApp(XmlApp xmlApp) {
    client().path("app").path(xmlApp.appId.toString()).put(xmlApp);
  }

  XmlApp getApp(int id) {
    return client().path("app").path(Integer.toString(id)).get(XmlApp.class);
  }

  private XmlApp someXmlApp() {
    XmlApp xmlApp = new XmlApp();
    xmlApp.appId = 345;
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
    Configuration cfg = injector().getInstance(Configuration.class);
    SchemaExport export = new SchemaExport(cfg);
    export.setOutputFile("/tmp/schema.sql");
    export.create(true, true);
    try {
      getApp(1);
      fail();
    } catch (UniformInterfaceException expected) {
      assertEquals(404, expected.getResponse().getStatus());
    }
  }
}
