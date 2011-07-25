package com.heymoose.rest.test;

import com.google.common.collect.Lists;
import com.heymoose.rest.resource.xml.XmlApp;
import com.heymoose.rest.resource.xml.XmlOrder;
import com.heymoose.rest.resource.xml.XmlProfile;
import com.heymoose.rest.resource.xml.XmlProfiles;
import com.heymoose.rest.resource.xml.XmlQuestion;
import com.heymoose.rest.resource.xml.XmlQuestions;
import com.heymoose.rest.resource.xml.XmlTargeting;
import com.heymoose.rest.test.base.RestTest;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ApiTest extends RestTest {

  void putApp(XmlApp xmlApp) {
    client().path("app").path(xmlApp.appId.toString()).put(xmlApp);
  }

  void sendProfiles(int appId, XmlProfiles xmlProfiles) {
    client().path("api").path("profiles").queryParam("app", Integer.toString(appId)).post(xmlProfiles);
  }

  XmlApp someXmlApp() {
    XmlApp xmlApp = new XmlApp();
    xmlApp.appId = 345;
    xmlApp.secret = "s";
    return xmlApp;
  }

  XmlProfiles someXmlProfiles() {
    XmlProfiles xmlProfiles = new XmlProfiles();
    xmlProfiles.profiles = Lists.newArrayList();
    XmlProfile xmlProfile = new XmlProfile();
    xmlProfile.profileId = "ext1";
    xmlProfiles.profiles.add(xmlProfile);
    return xmlProfiles;
  }
    
  private final static String ORDER_NAME = "Order1";
  private final static String CITY = "Moscow";
  private final static String BALANCE = "100.0";
  private final static String QUESTION_TEXT = "some question";
  
  int createOrder() {
    XmlTargeting targeting = new XmlTargeting();
    targeting.age = 20;
    targeting.male = true;
    targeting.city = CITY;
    targeting.country = "Russia";
    List<XmlQuestion> questions = Lists.newArrayList();
    XmlQuestion question = new XmlQuestion();
    question.text = QUESTION_TEXT;
    question.poll = false;
    questions.add(question);
    XmlOrder order = new XmlOrder();
    order.name = ORDER_NAME;
    order.balance = BALANCE;
    order.targeting = targeting;
    order.questions = questions;
    return Integer.valueOf(client().path("order").post(String.class, order));
  }

  XmlQuestions getQuestions(int appId, int count, String extId) {
    return client()
            .path("api")
            .path("questions")
            .queryParam("app", Integer.toString(appId))
            .queryParam("count", Integer.toString(count))
            .queryParam("extId", extId)
            .get(XmlQuestions.class);
  }

  @Test public void sendProfiles() {
    Configuration cfg = injector().getInstance(Configuration.class);
    SchemaExport export = new SchemaExport(cfg);
    export.setOutputFile("/tmp/schema.sql");
    export.create(true, true);

    XmlApp someApp = someXmlApp();
    putApp(someApp);
    XmlProfiles xmlProfiles = someXmlProfiles();
    sendProfiles(someApp.appId, xmlProfiles);
  }

  @Test public void getQuestions() {
    XmlApp someApp = someXmlApp();
    putApp(someApp);
    int orderId = createOrder();
    XmlQuestions xmlQuestions = getQuestions(someApp.appId, 1, "<UNUSED-PARAM>");
    assertEquals(1, xmlQuestions.questions.size());
    assertEquals(QUESTION_TEXT, xmlQuestions.questions.get(0).text);
    assertEquals(orderId, xmlQuestions.questions.get(0).orderId);
    assertEquals(false, xmlQuestions.questions.get(0).poll);
    xmlQuestions = getQuestions(someApp.appId, 0, "<UNUSED-PARAM>");
    assertEquals(null, xmlQuestions.questions);
  }
}
