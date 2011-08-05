package com.heymoose.rest.test;

import com.google.common.collect.Lists;
import com.heymoose.rest.resource.xml.XmlAnswer;
import com.heymoose.rest.resource.xml.XmlAnswers;
import com.heymoose.rest.resource.xml.XmlApp;
import com.heymoose.rest.resource.xml.XmlOrder;
import com.heymoose.rest.resource.xml.XmlProfile;
import com.heymoose.rest.resource.xml.XmlProfiles;
import com.heymoose.rest.resource.xml.XmlQuestion;
import com.heymoose.rest.resource.xml.XmlQuestions;
import com.heymoose.rest.resource.xml.XmlTargeting;
import com.heymoose.rest.test.base.RestTest;
import com.sun.jersey.api.client.WebResource;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ApiTest extends RestTest {

  WebResource apiClient;

  WebResource apiClient(int appId, String secret) {
    return client().path("api").queryParam("app", Integer.toString(appId)).queryParam("secret", secret);
  }  

  XmlApp someXmlApp() {
    XmlApp xmlApp = new XmlApp();
    xmlApp.appId = 345;
    xmlApp.secret = "s";
    return xmlApp;
  }

  void putApp(XmlApp xmlApp) {
    client().path("app").path(xmlApp.appId.toString()).put(xmlApp);
  }

  @Before
  public void createApiClient() {
    XmlApp xmlApp = someXmlApp();
    putApp(xmlApp);
    apiClient = apiClient(xmlApp.appId, xmlApp.secret);
  }


  void sendProfiles(XmlProfiles xmlProfiles) {
    apiClient.path("profiles").post(xmlProfiles);
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
  
  int createOrder(boolean form) {
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
    return Integer.valueOf(client().path("order").queryParam("form", Boolean.toString(form)).post(String.class, order));
  }

  XmlQuestions getQuestions(int count, String extId) {
    return apiClient
            .path("questions")
            .queryParam("count", Integer.toString(count))
            .queryParam("extId", extId)
            .get(XmlQuestions.class);
  }

  XmlQuestions getXmlForm(String extId) {
    return apiClient
            .path("form")
            .queryParam("extId", extId)
            .get(XmlQuestions.class);
  }

  void sendAnswers(XmlAnswers xmlAnswers) {
    apiClient.path("answers").post(xmlAnswers);
  }

  @Test public void sendProfiles() {
    sendProfiles(someXmlProfiles());
  }

  @Test public void getQuestions() {
    Configuration configuration = injector().getInstance(Configuration.class);
    SchemaExport export = new SchemaExport(configuration);
    export.setOutputFile("/tmp/schema.sql");
    export.create(true, false);
    XmlApp someApp = someXmlApp();
    XmlProfiles xmlProfiles = someXmlProfiles();
    sendProfiles(xmlProfiles);
    putApp(someApp);
    int orderId = createOrder(false);
    XmlQuestions xmlQuestions = getQuestions(1, xmlProfiles.profiles.get(0).profileId);
    assertEquals(1, xmlQuestions.questions.size());
    assertEquals(QUESTION_TEXT, xmlQuestions.questions.get(0).text);
    assertEquals(orderId, xmlQuestions.questions.get(0).orderId);
    assertEquals(false, xmlQuestions.questions.get(0).poll);
    xmlQuestions = getQuestions(1, xmlProfiles.profiles.get(0).profileId);
    assertEquals(null, xmlQuestions.questions);
  }

  @Test public void getForm() {
    XmlApp someApp = someXmlApp();
    putApp(someApp);
    int orderId = createOrder(true);
    XmlProfiles xmlProfiles = someXmlProfiles();
    sendProfiles(xmlProfiles);
    XmlQuestions xmlQuestions = getXmlForm(xmlProfiles.profiles.get(0).profileId);
    assertEquals(1, xmlQuestions.questions.size());
    assertEquals(QUESTION_TEXT, xmlQuestions.questions.get(0).text);
    assertEquals(orderId, xmlQuestions.questions.get(0).orderId);
    assertEquals(false, xmlQuestions.questions.get(0).poll);
  }

  @Test public void sendAnswers() {
    XmlProfiles xmlProfiles = someXmlProfiles();
    sendProfiles(xmlProfiles);

    createOrder(false);

    XmlQuestions xmlQuestions = getQuestions(1, xmlProfiles.profiles.get(0).profileId);

    XmlAnswers xmlAnswers = new XmlAnswers();
    xmlAnswers.answers = Lists.newArrayList();
    XmlAnswer xmlAnswer = new XmlAnswer();
    xmlAnswer.profileId = xmlProfiles.profiles.get(0).profileId;
    xmlAnswer.questionId = xmlQuestions.questions.get(0).id;
    xmlAnswer.text = "some answer";
    xmlAnswers.answers.add(xmlAnswer);

    sendAnswers(xmlAnswers);
  }
}
