package com.heymoose.test;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.user.Role;
import com.heymoose.test.base.RestTest;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

public class ClickTest extends RestTest {

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

  private long doCreateOffer(long advertiserId, boolean allowDeeplink) {
    sqlUpdate("insert into category_group(id, name) values(1, 'Grouping1')");
    sqlUpdate("insert into category(id, category_group_id, name) values(1, 1, 'Category1')");
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    long offerId = heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, CPA, null, OFFER_BALANCE,
        OFFER_NAME, "descr", "short descr", "logo", URI.create(OFFER_URL), URI.create(OFFER_SITE_URL), "title", false, false,
        true, newHashSet("RU"), newHashSet(categoryId), OFFER_CODE, 30, 180, DateTimeUtils.currentTimeMillis(), allowDeeplink);
    heymoose().approveOffer(offerId);
    return offerId;
  }

  private URI doClick(long offerId, long affId, String sourceId, Subs subs, String ulp) {
    sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");
    return heymoose().clickWithUlp(offerId, affId, sourceId, subs, ulp);
  }

  private void doCreateGrant(long offerId, long affId) {
    long grantId = heymoose().createGrant(offerId, affId, "msg", baseUrl() + "/postback");
    heymoose().unblockGrant(grantId);
    heymoose().approveGrant(grantId);
  }

  @Test
  public void click() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, false);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    URI location = doClick(offerId, affId, null, Subs.empty(), null);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickUlp() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, true);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String ulp = "http://www.google.com/a/b/c";
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(ulp).getHost(), location.getHost());
  }

  @Test
  public void clickUlpWhenDisallowed() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, false);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String ulp = "http://www.google.com/a/b/c";
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickUlpEncoded() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, true);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String s = "http://www.google.com/a/b/c";
    String ulp = null;
    try {
      ulp = URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException ignore) {
      //
    }
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(s).getHost(), location.getHost());
  }

  @Test
  public void clickUlpWithRedirect() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, true);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String ulp = "http://www.google.com/a/b/c?a=12&ulp=qwerty&c=98";
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(ulp).getHost(), location.getHost());
  }

  @Test
  public void clickBadUlp() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, true);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String ulp = "not an url";
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test

  public void clickOfferWithRequiredGetParams() throws Exception {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId, true);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    sqlUpdate(
        "update offer " +
        "set required_get_parameters = 'a=a&b=b' " +
        "where id = " + offerId);

    URI location = doClick(offerId, affId, null, Subs.empty(), null);

    log.info("Result URI: {}", location.toString());
    assertTrue(location.getQuery().contains("a=a"));
    assertTrue(location.getQuery().contains("b=b"));
  }
}
