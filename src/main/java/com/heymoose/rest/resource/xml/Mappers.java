package com.heymoose.rest.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.order.OrderBase;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.domain.question.QuestionBase;
import com.heymoose.rest.domain.question.Choice;
import com.heymoose.rest.domain.question.Poll;

public class Mappers {

  public static XmlApp toXmlApp(App app) {
    XmlApp xmlApp = new XmlApp();
    xmlApp.appId = app.id();
    xmlApp.secret = app.secret();
    return xmlApp;
  }

  public static XmlOrder toXmlOrder(OrderBase order) {
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

  public static XmlQuestions toXmlQuestions(Iterable<QuestionBase> questions) {
    XmlQuestions xmlQuestions = new XmlQuestions();
    xmlQuestions.questions = Lists.newArrayList();
    for (QuestionBase question : questions)
      xmlQuestions.questions.add(toXmlQuestion(question));
    return xmlQuestions;
  }

  public static XmlQuestion toXmlQuestion(QuestionBase question) {
    XmlQuestion xmlQuestion = new XmlQuestion();
    xmlQuestion.id = question.id();
    xmlQuestion.text = question.text();
    if (question.hasForm())
      xmlQuestion.orderId = question.form().order().id();
    else if (question.hashOrder())
      xmlQuestion.orderId = question.order().id();
    if (question instanceof Poll) {
      xmlQuestion.choices = Lists.newArrayList();
      Poll poll = (Poll) question;
      for (Choice c : poll.choices()) {
        XmlChoice xmlChoice = new XmlChoice();
        xmlChoice.id = c.id();
        xmlChoice.text = c.text();
        xmlQuestion.choices.add(xmlChoice);
      }
    }
    return xmlQuestion;
  }
}
