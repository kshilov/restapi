package com.heymoose.test;

import com.heymoose.domain.Platform;
import com.heymoose.domain.Role;
import com.heymoose.resource.xml.XmlAction;
import com.heymoose.resource.xml.XmlActions;
import com.heymoose.resource.xml.XmlApp;
import com.heymoose.resource.xml.XmlOffers;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
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
  boolean ALLOW_NEGATIVE_BALANCE = false;

  String APP_TITLE = "The App";
  String APP_URL = "http://example.org";
  String CALLBACK = "http://example.org/callback";
  Platform PLATFORM = Platform.FACEBOOK;

  XmlApp app;
  XmlUser user;

  @Before
  public void createApp() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.DEVELOPER);
    heymoose().createApp(APP_TITLE, userId, APP_URL, CALLBACK, PLATFORM);
    XmlUser user = heymoose().getUser(userId);
    app = user.apps.iterator().next();
    this.user = user;
  }

  long createOrder() {
    heymoose().addRoleToUser(user.id, Role.CUSTOMER);
    user = heymoose().getUser(user.id);
    heymoose().addToCustomerAccount(user.id, CUSTOMER_BALANCE);
    return heymoose().createRegularOrder(user.id, TITLE, DESCRIPTION, BODY, IMAGE, BALANCE, CPA, ALLOW_NEGATIVE_BALANCE);
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
    assertEquals(1, offers.offers.size());

    heymoose().approveAction(user, action.id);
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
    assertEquals(1, offers.offers.size());

    heymoose().deleteAction(action.id);
    actions = heymoose().getActions(0, Integer.MAX_VALUE);
    action = actions.actions.iterator().next();

    assertFalse(action.done);
    assertTrue(action.deleted);
    assertNull(action.approveTime);
  }

  @Test public void redirectIfOfferAcceptedAlready() {
    long orderId = createOrder();
    heymoose().approveOrder(orderId);
    XmlOffers offers = heymoose().getAvailableOffers(app, EXT_ID);
    assertEquals(1, offers.offers.size());
    long offerId = offers.offers.iterator().next().id;
    URI redirect1 = heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    URI redirect2 = heymoose().doOffer(app, offerId, EXT_ID, Platform.FACEBOOK);
    assertEquals(redirect1.getHost(), redirect2.getHost());
    assertEquals(URI.create(BODY).getHost(), redirect2.getHost());
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
    assertEquals(1, offers.offers.size());

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
