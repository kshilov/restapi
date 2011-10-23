package com.heymoose.test;

import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlOrder;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderTest extends RestTest {

  String EMAIL = "test@heymoose.com";
  String NICKNAME = "anon";
  String PASSWORD_HASH = "3gewn4iougho";

  double CUSTOMER_BALANCE = 30.0;

  String TITLE = "body";
  String DESCRIPTION = "description";
  String BODY = "http://ya.ru";
  String IMAGE = "sdfasdfnaslf";
  double BALANCE = 20.0;
  double CPA = 2.0;

  long createAndReturnUserId() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, CUSTOMER_BALANCE);
    heymoose().createOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA);
    return userId;
  }

  void validateNewOrder(XmlOrder order) {
    assertEquals(TITLE, order.title);
    assertEquals(Double.valueOf(BALANCE), Double.valueOf(order.balance));
    assertFalse(order.approved);
    assertFalse(order.deleted);
  }

  @Test public void createOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    assertEquals(Double.valueOf(CUSTOMER_BALANCE - BALANCE), Double.valueOf(user.customerAccount));
    assertEquals(1, user.orders.size());
    assertNotNull(user.customerSecret);
    XmlOrder order = user.orders.iterator().next();
    validateNewOrder(order);
  }

  @Test public void createOrderWithoutCustomerRole() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    try {
      heymoose().createOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(409, e.getResponse().getStatus());
    }
  }

  @Test public void createOrderWithNoEnoughMoney() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, 10.0);
    try {
      heymoose().createOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(409, e.getResponse().getStatus());
    }
  }

  @Test public void getOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    long orderId = user.orders.iterator().next().id;
    XmlOrder order = heymoose().getOrder(orderId);
    validateNewOrder(order);
  }

  @Test public void getNonExistentOrder() {
    try {
      heymoose().getOrder(1L);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(404, e.getResponse().getStatus());
    }
  }

  @Test public void approveOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    long orderId = user.orders.iterator().next().id;
    heymoose().approveOrder(orderId);
    XmlOrder order = heymoose().getOrder(orderId);
    assertTrue(order.approved);
  }

  @Test public void deleteOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    long orderId = user.orders.iterator().next().id;
    heymoose().deleteOrder(orderId);
    try {
      heymoose().getOrder(orderId);
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(404, e.getResponse().getStatus());
    }
  }
}
