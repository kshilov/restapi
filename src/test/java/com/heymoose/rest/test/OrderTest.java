package com.heymoose.rest.test;

import com.google.common.collect.Lists;
import com.heymoose.rest.resource.xml.XmlOrder;
import com.heymoose.rest.resource.xml.XmlQuestion;
import com.heymoose.rest.resource.xml.XmlTargeting;
import com.heymoose.rest.test.base.RestTest;
import com.sun.jersey.api.representation.Form;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class OrderTest extends RestTest {

  private final static String ORDER_NAME = "Order1";
  private final static String CITY = "Moscow";
  private final static String BALANCE = "10.0";

  int createOrder(boolean questionary) {
    XmlTargeting targeting = new XmlTargeting();
    targeting.age = 20;
    targeting.male = true;
    targeting.city = CITY;
    targeting.country = "Russia";
    List<XmlQuestion> questions = Lists.newArrayList();
    XmlQuestion question = new XmlQuestion();
    question.text = "some question";
    question.poll = false;
    questions.add(question);
    XmlOrder order = new XmlOrder();
    order.name = ORDER_NAME;
    order.balance = BALANCE;
    order.targeting = targeting;
    order.questions = questions;
    return Integer.valueOf(client().path("order").post(String.class, order));
  }

  XmlOrder getOrder(int id) {
    return client().path("order").path(Integer.toString(id)).get(XmlOrder.class);
  }

  @Test public void create() {
    int orderId = createOrder(false);
    XmlOrder saved = getOrder(orderId);
    assertEquals(orderId, saved.id);
    assertEquals(ORDER_NAME, saved.name);
    assertEquals(CITY, saved.targeting.city);
  }

  @Test public void addToBalance() {
    int orderId = createOrder(false);
    String amount = "5.0";
    Form form = new Form();
    form.add("amount", amount);
    client().path("order").path(Integer.toString(orderId)).path("balance").post(form);
    XmlOrder xmlOrder = getOrder(orderId);
    assertEquals(
            new BigDecimal(BALANCE).add(new BigDecimal(amount)).setScale(2).toString(),
            new BigDecimal(xmlOrder.balance).setScale(2).toString()
    );
  }
}
