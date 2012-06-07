package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.URLEncodedUtils;
import java.net.URI;
import java.util.List;
import org.joda.time.DateTimeUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class OfferTest extends RestTest {

  private final static String OFFER_CODE = "offer324234";
  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_SITE_URL = "http://yandex.ru";
  private final static String OFFER_NAME = "Offer1";
  private final static double ADV_BALANCE = 100.0;
  private final static double OFFER_BALANCE = 70.0;
  private final static double CPA = 30.0;

  @Before
  public void before() {
    reset();
  }

  private long doRegisterAdvertiser() {
    long advertiserId = heymoose().registerUser("u@u.ru", "ads", "F", "L", "777");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, ADV_BALANCE);
    return advertiserId;
  }

  private long doRegisterAffiliate() {
    long affId = heymoose().registerUser("af1@af.ru", "dsfs", "F", "L", "777");
    heymoose().addRoleToUser(affId, Role.AFFILIATE);
    heymoose().confirmUser(affId);
    return affId;
  }

  private long doCreateOffer(long advertiserId) {
    sqlUpdate("insert into category(id, grouping, name) values(1, 'Group1', 'Category1')");
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    long offerId = heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, CPA, OFFER_BALANCE,
        OFFER_NAME, "descr", "short descr", "logo", URI.create(OFFER_URL), URI.create(OFFER_SITE_URL), "title", false, false,
        true, newHashSet(Region.RUSSIA), newHashSet(categoryId), OFFER_CODE, 30, 180, DateTimeUtils.currentTimeMillis());
    heymoose().approveOffer(offerId);
    return offerId;
  }

  private URI doClick(long offerId, long affId, Subs subs) {
    sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");
    URI location = heymoose().click(offerId, affId, subs);
    return location;
  }

  private void doCreateGrant(long offerId, long affId) {
    long grantId = heymoose().createGrant(offerId, affId, "msg", baseUrl() + "/postback");
    heymoose().unblockGrant(grantId);
    heymoose().approveGrant(grantId);
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
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, Subs.empty()));
  }

  @Test
  public void clickEmptySubs() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, Subs.empty()));
    URI location = doClick(offerId, affId, Subs.empty());
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickSourceId() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-sourceId", null, null, null, null, null);
    assertEquals(200, heymoose().track(offerId, affId, subs));
    URI location = doClick(offerId, affId, subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickSourceIdAndOneSubId() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-sourceId", "test-subId", null, null, null, null);
    assertEquals(200, heymoose().track(offerId, affId, subs));
    URI location = doClick(offerId, affId, subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickSourceIdAndThreeSubId() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-sourceId", "test-subId", null, "test-subId3", null, "test-subId5");
    assertEquals(200, heymoose().track(offerId, affId, subs));
    URI location = doClick(offerId, affId, subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickDifferentSubsOnTrackAndClick() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    Subs subs = new Subs("test-sourceId", "test-subId", null, null, null, null);
    assertEquals(200, heymoose().track(offerId, affId, subs));

    subs = new Subs("test-sourceId", "wrong-test-subId", null, null, null, null);
    URI location = doClick(offerId, affId, subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());

    subs = new Subs("test-sourceId", "test-subId", null, "another-test-sub3", null, null);
    location = doClick(offerId, affId, subs);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void action() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    URI location = doClick(offerId, affId, Subs.empty());
    String token = extractParams(URLEncodedUtils.parse(location, "UTF-8"), "_hm_token");
    assertEquals(200, heymoose().action(token, "tx1", advertiserId, OFFER_CODE));
    assertEquals(OFFER_BALANCE - CPA, heymoose().getOffer(offerId).account.balance, 0.000001);
    XmlUser aff = heymoose().getUser(affId);
    int fee = aff.fee;
    assertEquals(CPA * (100 - fee) / 100.0, aff.affiliateAccountNotConfirmed.balance, 0.000001);
  }

  @Test
  public void actionWithSubs() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    URI location = doClick(offerId, affId,
        new Subs("test-sourceId", "test-subId", null, "test-subId3", null, "test-subId5")
    );
    String token = extractParams(URLEncodedUtils.parse(location, "UTF-8"), "_hm_token");
    assertEquals(200, heymoose().action(token, "tx1", advertiserId, OFFER_CODE));
    assertEquals(OFFER_BALANCE - CPA, heymoose().getOffer(offerId).account.balance, 0.000001);
    XmlUser aff = heymoose().getUser(affId);
    int fee = aff.fee;
    assertEquals(CPA * (100 - fee) / 100.0, aff.affiliateAccountNotConfirmed.balance, 0.000001);
  }

  private static String extractParams(List<NameValuePair> pairs, String param) {
    for (NameValuePair pair : pairs)
      if (pair.fst.equals(param))
        return pair.snd;
    return null;
  }
}
