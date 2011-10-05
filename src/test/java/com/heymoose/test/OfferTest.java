package com.heymoose.test;

import com.heymoose.domain.Offer;
import com.heymoose.domain.Role;
import com.heymoose.resource.OfferResource;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import org.junit.Ignore;
import org.junit.Test;

public class OfferTest extends RestTest {

  String EXT_ID = "ext1";

  String EMAIL = "test@heymoose.com";
  String NICKNAME = "anon";
  String PASSWORD_HASH = "3gewn4iougho";

  double CUSTOMER_BALANCE = 30.0;

  String TITLE = "body";
  String BODY = "http://ya.ru";
  double BALANCE = 20.0;
  double CPA = 2.0;

  long createOfferAndReturnUserId() {
    long userId = heymoose().registerUser(EMAIL, NICKNAME, PASSWORD_HASH);
    heymoose().addRoleToUser(userId, Role.CUSTOMER);
    heymoose().addToCustomerAccount(userId, CUSTOMER_BALANCE);
    heymoose().createOrder(userId, TITLE, BODY, BALANCE, CPA);
    return userId;
  }

  @Ignore
  @Test public void getAvailableOffers() {
    long userId = createOfferAndReturnUserId();
    XmlUser user = heymoose().getUser(userId);
    long orderId = user.orders.iterator().next().id;
    heymoose().approveOrder(orderId);
    OfferResource offers = injector().getInstance(OfferResource.class);
    Iterable<Offer> availableOffers =  offers.getAvailableOffers(EXT_ID);
    log.info(availableOffers.toString());
  }
}
