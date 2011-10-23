package com.heymoose.test;

import com.heymoose.domain.Platform;
import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlAction;
import com.heymoose.resource.xml.XmlActions;
import com.heymoose.resource.xml.XmlApp;
import com.heymoose.resource.xml.XmlOffers;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OfferTest extends RestTest {

  String EXT_ID = "ext1";

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

  String CALLBACK = "http://example.org/callback";

  XmlApp app;
  long userId;

  @Before
  public void createApp() {
    userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.DEVELOPER);
    heymoose().createApp(userId, CALLBACK);
    XmlUser user = heymoose().getUser(userId);
    app = user.app;
  }

  long createOrder() {
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, CUSTOMER_BALANCE);
    return heymoose().createOrder(userId, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA);
  }

  @Test public void getAvailableOffersForUnknownPerformer() {
    XmlOffers offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(0, offers.offers.size());
    long orderId = createOrder();
    offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(0, offers.offers.size());
    heymoose().approveOrder(orderId);
    offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
  }

  @Test public void doOffer() {
    long orderId = createOrder();
    heymoose().approveOrder(orderId);
    XmlOffers offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
    long offerId = offers.offers.iterator().next().id;
    URI redirect = heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    assertEquals(URI.create(BODY).getHost(), redirect.getHost());

    XmlActions actions = heymoose().getActions(0, Integer.MAX_VALUE);
    assertEquals(1, actions.actions.size());
    XmlAction action = actions.actions.iterator().next();
    assertEquals(Long.valueOf(offerId), Long.valueOf(action.offerId));
    assertFalse(action.done);
    assertFalse(action.deleted);
    assertNull(action.approveTime);

    offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(0, offers.offers.size());

    heymoose().approveAction(action.id);
    actions = heymoose().getActions(0, Integer.MAX_VALUE);
    action = actions.actions.iterator().next();

    assertTrue(action.done);
    assertFalse(action.deleted);
    assertNotNull(action.approveTime);
  }

  @Test public void deleteAction() {
    long orderId = createOrder();
    heymoose().approveOrder(orderId);
    XmlOffers offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
    long offerId = offers.offers.iterator().next().id;
    URI redirect = heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    assertEquals(URI.create(BODY).getHost(), redirect.getHost());

    XmlActions actions = heymoose().getActions(0, Integer.MAX_VALUE);
    assertEquals(1, actions.actions.size());
    XmlAction action = actions.actions.iterator().next();
    assertEquals(Long.valueOf(offerId), Long.valueOf(action.offerId));
    assertFalse(action.done);
    assertFalse(action.deleted);
    assertNull(action.approveTime);

    offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(0, offers.offers.size());

    heymoose().deleteAction(action.id);
    actions = heymoose().getActions(0, Integer.MAX_VALUE);
    action = actions.actions.iterator().next();

    assertFalse(action.done);
    assertTrue(action.deleted);
    assertNull(action.approveTime);
  }

  @Test public void failIfOfferAcceptedAlready() {
    long orderId = createOrder();
    heymoose().approveOrder(orderId);
    XmlOffers offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
    long offerId = offers.offers.iterator().next().id;
    heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    try {
      heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK).toString();
      fail();
    } catch (UniformInterfaceException e) {
      assertEquals(409, e.getResponse().getStatus());
    }
  }

  @Test public void redoOfferAfterActionDeleting() {
    long orderId = createOrder();
    heymoose().approveOrder(orderId);
    XmlOffers offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
    long offerId = offers.offers.iterator().next().id;
    URI redirect = heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    assertEquals(URI.create(BODY).getHost(), redirect.getHost());

    XmlActions actions = heymoose().getActions(0, Integer.MAX_VALUE);
    assertEquals(1, actions.actions.size());
    XmlAction action = actions.actions.iterator().next();
    assertEquals(Long.valueOf(offerId), Long.valueOf(action.offerId));
    assertFalse(action.done);
    assertFalse(action.deleted);
    assertNull(action.approveTime);

    offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(0, offers.offers.size());

    heymoose().deleteAction(action.id);
    actions = heymoose().getActions(0, Integer.MAX_VALUE);
    action = actions.actions.iterator().next();

    assertFalse(action.done);
    assertTrue(action.deleted);
    assertNull(action.approveTime);

    offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
    offerId = offers.offers.iterator().next().id;
    redirect = heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    assertEquals(URI.create(BODY).getHost(), redirect.getHost());
  }
}
