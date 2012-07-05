package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.test.base.RestTest;
import com.heymoose.util.URIUtils;
import com.heymoose.util.URLEncodedUtils;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import org.joda.time.DateTimeUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

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
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    URI location = doClick(offerId, affId, null, Subs.empty(), null);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }

  @Test
  public void clickUlp() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String ulp = "http://www.google.com/a/b/c";
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(ulp).getHost(), location.getHost());
  }

  @Test
  public void clickUlpEncoded() {
    long advertiserId = doRegisterAdvertiser();
    long offerId = doCreateOffer(advertiserId);
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
    long offerId = doCreateOffer(advertiserId);
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
    long offerId = doCreateOffer(advertiserId);
    long affId = doRegisterAffiliate();
    doCreateGrant(offerId, affId);
    assertEquals(200, heymoose().track(offerId, affId, null, Subs.empty()));
    String ulp = "not an url";
    URI location = doClick(offerId, affId, null, Subs.empty(), ulp);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }
}
