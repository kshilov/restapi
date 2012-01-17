package com.heymoose.test;

import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlBannerSize;
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
  String MIME_TYPE = "image/png";
  String VIDEO_URL = "http://video.com/video1";
  double BALANCE = 20.0;
  double CPA = 2.0;
  boolean ALLOW_NEGATIVE_BALANCE = false;

  long createAndReturnUserId() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, CUSTOMER_BALANCE);
    heymoose().createRegularOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA, ALLOW_NEGATIVE_BALANCE);
    return userId;
  }

  long createVideoAndReturnUserId() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, CUSTOMER_BALANCE);
    heymoose().createVideoOrder(userId, TITLE, VIDEO_URL, BODY, BALANCE, CPA, ALLOW_NEGATIVE_BALANCE);
    return userId;
  }

  long createBannerAndReturnUserId() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, CUSTOMER_BALANCE);
    long size = heymoose().bannerSize(468, 60);
    heymoose().createBannerOrder(userId, TITLE, BODY, IMAGE, MIME_TYPE, size, BALANCE, CPA, ALLOW_NEGATIVE_BALANCE);
    return userId;
  }

  void validateNewOrder(XmlOrder order) {
    assertEquals(TITLE, order.title);
    assertEquals(Double.valueOf(BALANCE), Double.valueOf(order.balance));
    assertTrue(order.disabled);
  }

  @Test public void createOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    assertEquals(Double.valueOf(CUSTOMER_BALANCE - BALANCE), user.customerAccount.balance);
    assertEquals(1, user.orders.size());
    assertNotNull(user.customerSecret);
    XmlOrder order = user.orders.iterator().next();
    validateNewOrder(order);
  }

  @Test public void createOrderWithVideoOffer() {
    long userId = createVideoAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    assertEquals(Double.valueOf(CUSTOMER_BALANCE - BALANCE), user.customerAccount.balance);
    assertEquals(1, user.orders.size());
    assertNotNull(user.customerSecret);
    XmlOrder order = user.orders.iterator().next();
    validateNewOrder(order);
    assertEquals(VIDEO_URL, order.videoUrl);
  }

  @Test public void createOrderWithBannerOffer() {
    long userId = createBannerAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    assertEquals(Double.valueOf(CUSTOMER_BALANCE - BALANCE), user.customerAccount.balance);
    assertEquals(1, user.orders.size());
    assertNotNull(user.customerSecret);
    XmlOrder order = user.orders.iterator().next();
    validateNewOrder(order);
    assertEquals(IMAGE, order.imageBase64);
    Long size = heymoose().bannerSize(468, 60);
    assertEquals(size, order.bannerSize.id);
  }

  @Test public void createOrderWithoutCustomerRole() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    try {
      heymoose().createRegularOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA, ALLOW_NEGATIVE_BALANCE);
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
      heymoose().createRegularOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA, ALLOW_NEGATIVE_BALANCE);
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

  @Test public void enableOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    long orderId = user.orders.iterator().next().id;
    heymoose().approveOrder(orderId);
    XmlOrder order = heymoose().getOrder(orderId);
    assertFalse(order.disabled);
  }

  @Test public void disableOrder() {
    long userId = createAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    long orderId = user.orders.iterator().next().id;
    heymoose().disableOrder(orderId);
    XmlOrder order = heymoose().getOrder(orderId);
    assertTrue(order.disabled);
  }

  @Test public void bannerSizes() {
    assertEquals(0, heymoose().bannerSizes().bannerSizes.size());
    heymoose().bannerSize(468, 60);
    assertEquals(1, heymoose().bannerSizes().bannerSizes.size());
    heymoose().bannerSize(468, 60);
    assertEquals(1, heymoose().bannerSizes().bannerSizes.size());
    XmlBannerSize size = heymoose().bannerSizes().bannerSizes.get(0);
    assertEquals(Integer.valueOf(468), size.width);
    assertEquals(Integer.valueOf(60), size.height);
    heymoose().bannerSize(468, 120);
    assertEquals(2, heymoose().bannerSizes().bannerSizes.size());
  }
}
