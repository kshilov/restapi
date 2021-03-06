package com.heymoose.test;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.user.Role;
import com.heymoose.infrastructure.util.QueryUtil;
import com.heymoose.infrastructure.util.URLEncodedUtils;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

public class OfferTest extends RestTest {

  private final static String OFFER_CODE = "offer324234";
  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_SITE_URL = "http://yandex.ru";
  private final static String OFFER_NAME = "Offer1";
  private final static double ADV_BALANCE = 1000.0;
  private final static double OFFER_BALANCE = 700.0;
  private final static double CPA = 130.0;

  @Before
  public void before() {
    reset();
  }

  private long doRegisterAdvertiser() {
    long advertiserId = heymoose().registerUser("u@u.ru", "ads");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, ADV_BALANCE);
    return advertiserId;
  }

  private long doRegisterAffiliate() {
    long affId = heymoose().registerUser("af1@af.ru", "dsfs");
    heymoose().addRoleToUser(affId, Role.AFFILIATE);
    heymoose().confirmUser(affId);
    return affId;
  }

  private long doCreateOffer(long advertiserId) {
    sqlUpdate("insert into category_group(id, name) values(1, 'Group1')");
    sqlUpdate("insert into category(id, category_group_id, name) values(1, 1, 'Category1')");
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    long offerId = heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, CPA, null, OFFER_BALANCE,
        OFFER_NAME, "descr", "short descr", "logo", URI.create(OFFER_URL), URI.create(OFFER_SITE_URL), "title", false, false,
        true, newHashSet("RU"), newHashSet(categoryId), OFFER_CODE, 30, 180, DateTimeUtils.currentTimeMillis(), false);
    heymoose().approveOffer(offerId);
    return offerId;
  }

  private URI doClick(long offerId, long affId, String sourceId, Subs subs) {
    sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");
    URI location = heymoose().click(offerId, affId, sourceId, subs);
    return location;
  }

  @Test
  public void createOffer() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    XmlOffer offer = heymoose().getOffer(offerId);
    assertEquals(OFFER_NAME, offer.name);
  }

  @Test
  public void trackGrant() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
  }

  @Test
  public void clickEmptySubs() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    URI location = doClick(offerId, affId, null, Subs.empty());
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickSourceId() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    Subs subs = new Subs(null, null, null, null, null);
    assertEquals(200, heymoose().track(offerId, affId, "test-source-id", subs));
    URI location = doClick(offerId, affId, "test-source-id", subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickSourceIdAndOneSubId() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-subId", null, null, null, null);
    assertEquals(200, heymoose().track(offerId, affId, "test-sourceId", subs));
    URI location = doClick(offerId, affId, "test-sourceId", subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickSourceIdAndThreeSubId() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-subId", null, "test-subId3", null, "test-subId5");
    assertEquals(200, heymoose().track(offerId, affId, "test-sourceId", subs));
    URI location = doClick(offerId, affId, "test-sourceId", subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickDifferentSubsOnTrackAndClick() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-subId", null, null, null, null);
    assertEquals(200, heymoose().track(offerId, affId, "test-sourceId", subs));

    subs = new Subs("wrong-test-subId", null, null, null, null);
    URI location = doClick(offerId, affId, "test-sourceId", subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());

    subs = new Subs("test-subId", null, "another-test-sub3", null, null);
    location = doClick(offerId, affId, "test-sourceId", subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void action() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    URI location = doClick(offerId, affId, null, Subs.empty());
    String token = QueryUtil.extractParam(URLEncodedUtils.parse(location, "UTF-8"), "_hm_token");
    assertEquals(200, heymoose().action(token, "tx1", advertiserId, OFFER_CODE));
    assertEquals(OFFER_BALANCE - CPA, heymoose().getOffer(offerId).account.balance, 0.000001);
    XmlUser aff = heymoose().getUser(affId);
    XmlOffer offer = heymoose().getOffer(offerId);
    double affiliateCost = offer.affiliateCost.doubleValue();
    assertEquals(affiliateCost, aff.affiliateAccountNotConfirmed.balance, 0.000001);
  }

  @Test
  public void actionWithSubs() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    heymoose().doCreateGrant(offerId, affId);
    URI location = doClick(offerId, affId, "test-sourceId",
        new Subs("test-subId", null, "test-subId3", null, "test-subId5")
    );
    String token = QueryUtil.extractParam(URLEncodedUtils.parse(location, "UTF-8"), "_hm_token");
    assertEquals(200, heymoose().action(token, "tx1", advertiserId, OFFER_CODE));
    assertEquals(OFFER_BALANCE - CPA, heymoose().getOffer(offerId).account.balance, 0.000001);
    XmlUser aff = heymoose().getUser(affId);
    double affiliateCost = heymoose().getOffer(offerId).affiliateCost.doubleValue();
    assertEquals(affiliateCost, aff.affiliateAccountNotConfirmed.balance, 0.000001);
  }
}
