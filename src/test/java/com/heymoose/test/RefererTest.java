package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.test.base.RestTest;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.Pair;
import com.heymoose.util.URLEncodedUtils;
import java.net.URI;
import java.util.List;
import java.util.Random;
import org.joda.time.DateTimeUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RefererTest extends RestTest {

  private final static String OFFER_CODE = "offer324234";
  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_SITE_URL = "http://yandex.ru";
  private final static String OFFER_NAME = "Offer1";
  private final static double ADV_BALANCE = 1000.0;
  private final static double OFFER_BALANCE = 10.0;
  private final static double CPA = 30.0;

  private static final Random rnd = new Random(System.currentTimeMillis());

  private static Long affId = -1L;
  private static Long lastOfferId = -1L;

  private static long doRegisterAdvertiser() {
    long advertiserId = heymoose().registerUser("u@u.ru", "ads");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, ADV_BALANCE);
    return advertiserId;
  }

  private static long doRegisterAffiliate() {
    long affId = heymoose().registerUser("af1@af.ru", "dsfs");
    heymoose().addRoleToUser(affId, Role.AFFILIATE);
    heymoose().confirmUser(affId);
    return affId;
  }

  private static Pair<Long, String> doCreateOffer(long advertiserId, int seed) {
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    String offerCode = OFFER_CODE + "-" + seed + "-" + rnd.nextInt(1000);
    long offerId = heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, CPA, OFFER_BALANCE,
        OFFER_NAME, "descr", "short descr", "logo", URI.create(OFFER_URL), URI.create(OFFER_SITE_URL),
        "title", false, false, true, newHashSet(Region.RUSSIA), newHashSet(categoryId),
        offerCode, 30, 180, DateTimeUtils.currentTimeMillis());
    heymoose().approveOffer(offerId);
    return new Pair<Long, String>(offerId, offerCode);
  }

  private static URI doClick(long offerId, long affId, String sourceId, Subs subs, String referer) {
    return heymoose().click(offerId, affId, sourceId, subs, referer);
  }

  private static void doCreateGrant(long offerId, long affId) {
    long grantId = heymoose().createGrant(offerId, affId, "msg", baseUrl() + "/postback");
    heymoose().unblockGrant(grantId);
    heymoose().approveGrant(grantId);
  }

  private static String extractParams(List<NameValuePair> pairs, String param) {
    for (NameValuePair pair : pairs)
      if (pair.fst.equals(param))
        return pair.snd;
    return null;
  }

  @BeforeClass
  public static void beforeClass() {
    reset();

    sqlUpdate("insert into category(id, grouping, name) values(1, 'Group1', 'Category1')");
    sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");

    long advertiserId = doRegisterAdvertiser();
    affId = doRegisterAffiliate();

    for (int i = 0; i < 3; i++) { // 3 offers
      // create offer
      Pair<Long, String> createdOfferPair = doCreateOffer(advertiserId, i);
      long offerId = (lastOfferId = createdOfferPair.fst);
      String offerCode = createdOfferPair.snd;
      doCreateGrant(offerId, affId);

      // 5 shows
      for (int j = 0; j < 5; j++) {
        int k = rnd.nextInt(3);
        String sourceId = k + "-sourceId";
        Subs subs = new Subs(k + "-subId", null, k + "-subId2", null, k + "-subId4");
        // show
        assertEquals(200, heymoose().track(offerId, affId, sourceId, subs));
      }

      // 5 more shows with clicks
      for (int j = 0; j < 5; j++) {
        int k = rnd.nextInt(3);
        String sourceId = k + "-sourceId";
        Subs subs = new Subs(k + "-subId", null, k + "-subId2", null, k + "-subId4");

        // show
        assertEquals(200, heymoose().track(offerId, affId, sourceId, subs));

        // click
        String referer = rnd.nextInt(7) > 4 ? null :
            "http://" + (rnd.nextInt(7) > 4 ? "rambler.ru" : "yandex.ru")
                + "/search?" + (rnd.nextInt(7) > 4 ? "q" : "text") + "=" + k + "qwerty";
        URI location = doClick(offerId, affId, sourceId, subs, referer);
        assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
      }

      // 5 more shows with clicks and actions
      for (int j = 0; j < 5; j++) {
        int k = rnd.nextInt(3);
        String sourceId = k + "-sourceId";
        Subs subs = new Subs(k + "-subId", null, k + "-subId2", null, k + "-subId4");

        // show
        assertEquals(200, heymoose().track(offerId, affId, sourceId, subs));

        // click
        String referer = rnd.nextInt(7) > 4 ? null :
            "http://" + (rnd.nextInt(7) > 4 ? "rambler.ru" : "yandex.ru")
                + "/search?" + (rnd.nextInt(7) > 4 ? "q" : "text") + "=" + k + "qwerty";
        URI location = doClick(offerId, affId, sourceId, subs, referer);
        assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());

        // action
        String token = extractParams(URLEncodedUtils.parse(location, "UTF-8"), "_hm_token");
        assertEquals(200, heymoose().action(token, "tx1", advertiserId, offerCode));
      }
    }

    // flush buffered data
    heymoose().flushBufferedCounters();
  }

  private OverallOfferStatsList stats;

  @Test
  public void getStats() {
    stats = heymoose().getOffersAllStats(true, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getRefererStats() {
    stats = heymoose().getRefererStats(true, null, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getRefererStatsWithOffer() {
    stats = heymoose().getRefererStats(true, null, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getRefererStatsForAff() {
    stats = heymoose().getRefererStats(true, affId, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getRefererStatsForAffWithOffer() {
    stats = heymoose().getRefererStats(true, affId, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getKeywordsStats() {
    stats = heymoose().getKeywordsStats(true, null, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getKeywordsStatsWithOffer() {
    stats = heymoose().getKeywordsStats(true, null, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getKeywordsStatsForAff() {
    stats = heymoose().getKeywordsStats(true, affId, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getKeywordsStatsForAffWithOffer() {
    stats = heymoose().getKeywordsStats(true, affId, lastOfferId);
    assertNotNull(stats.stats);
  }
}
