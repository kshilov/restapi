package com.heymoose.rest.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.Question;
import com.heymoose.rest.domain.order.Targeting;

import java.util.List;

public class Mappers {

  public static XmlApp toXmlApp(App app) {
    XmlApp xmlApp = new XmlApp();
    xmlApp.appId = app.id();
    xmlApp.secret = app.secret();
    return xmlApp;
  }

  public static XmlOrder toXmlOrder(Order order) {
    XmlOrder xmlOrder = new XmlOrder();
    xmlOrder.id = order.id();
    xmlOrder.name = order.name();
    xmlOrder.balance = order.balance().toString();
    xmlOrder.targeting = toXmlTargeting(order.targeting());
    return xmlOrder;
  }

  public static XmlTargeting toXmlTargeting(Targeting targeting) {
    XmlTargeting xmlTargeting = new XmlTargeting();
    xmlTargeting.age = targeting.age();
    xmlTargeting.male = targeting.male();
    xmlTargeting.city = targeting.city();
    xmlTargeting.country = targeting.county();
    return xmlTargeting;
  }

  public static XmlQuestions toXmlQuestions(Iterable<Question> questions) {
    List<XmlQuestion> xmlQuestionsList = Lists.newArrayList();
    for (Question question : questions)
      xmlQuestionsList.add(toXmlQuestion(question));
    XmlQuestions xmlQuestions = new XmlQuestions();
    xmlQuestions.questions = xmlQuestionsList;
    return xmlQuestions;
  }

  public static XmlQuestion toXmlQuestion(Question question) {
    XmlQuestion xmlQuestion = new XmlQuestion();
    xmlQuestion.text = question.text();
    xmlQuestion.orderId = question.order().id();
    return xmlQuestion;
  }
}
