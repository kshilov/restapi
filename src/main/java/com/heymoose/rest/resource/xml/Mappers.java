package com.heymoose.rest.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.poll.BaseQuestion;

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
    xmlOrder.balance = order.account().actual().balance().toString();
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

  public static List<XmlQuestion> toXmlQuestions(Iterable<BaseQuestion> questions) {
    List<XmlQuestion> xmlQuestionsList = Lists.newArrayList();
    for (BaseQuestion question : questions)
      xmlQuestionsList.add(toXmlQuestion(question));
    return xmlQuestionsList;
  }

  public static XmlQuestion toXmlQuestion(BaseQuestion question) {
    XmlQuestion xmlQuestion = new XmlQuestion();
    xmlQuestion.text = question.text();
    xmlQuestion.orderId = question.order().id();
    return xmlQuestion;
  }
}
