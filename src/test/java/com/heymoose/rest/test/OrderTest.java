package com.heymoose.rest.test;

import com.heymoose.rest.resource.xml.XmlOrder;
import com.heymoose.rest.resource.xml.XmlTargeting;
import com.heymoose.rest.test.base.ApiTest;
import com.sun.jersey.api.representation.Form;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class OrderTest extends ApiTest {

  private final static String ORDER_NAME = "Order1";
  private final static String CITY = "Moscow";
  private final static String BALANCE = "10.0";

  int createOrder() {
    XmlTargeting targeting = new XmlTargeting();
    targeting.age = 20;
    targeting.male = true;
    targeting.city = CITY;
    targeting.country = "Russia";
    XmlOrder order = new XmlOrder();
    order.name = ORDER_NAME;
    order.balance = BALANCE;
    order.targeting = targeting;
    return Integer.valueOf(client().path("order").post(String.class, order));
  }

  XmlOrder getOrder(int id) {
    return client().path("order").path(Integer.toString(id)).get(XmlOrder.class);
  }

  @Test public void create() {
    int orderId = createOrder();
    XmlOrder saved = getOrder(orderId);
    assertEquals(orderId, saved.id);
    assertEquals(ORDER_NAME, saved.name);
    assertEquals(CITY, saved.targeting.city);
  }

  @Test public void addToBalance() {
    int orderId = createOrder();
    String amount = "5.0";
    Form form = new Form();
    form.add("amount", amount);
    client().path("order").path(Integer.toString(orderId)).path("balance").post(form);
    XmlOrder xmlOrder = getOrder(orderId);
    assertEquals(
            new BigDecimal(BALANCE).add(new BigDecimal(amount)).longValue(),
            new BigDecimal(xmlOrder.balance).longValue()
    );
  }
}
