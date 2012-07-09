package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.test.base.RestTest;
import com.heymoose.util.Pair;
import com.heymoose.util.QueryUtil;
import com.heymoose.util.URLEncodedUtils;
import java.net.URI;
import java.util.Random;
import org.joda.time.DateTimeUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class OfferStatTest extends RestTest {

  private final static String OFFER_CODE = "offer324234";
  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_SITE_URL = "http://yandex.ru";
  private final static String OFFER_NAME = "Offer1";
  private final static double ADV_BALANCE = 1000.0;
  private final static double OFFER_BALANCE = 10.0;
  private final static double CPA = 30.0;

  private static final Random rnd = new Random(System.currentTimeMillis());

  private static Long advertiserId = 0L;
  private static Long affId = 0L;
  private static Long lastOfferId = 0L;

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
        "title", false, false, true, newHashSet("RU"), newHashSet(categoryId),
        offerCode, 30, 180, DateTimeUtils.currentTimeMillis(), false);
    heymoose().approveOffer(offerId);
    return new Pair<Long, String>(offerId, offerCode);
  }

  private static URI doClick(long offerId, long affId, String sourceId, Subs subs) {
    return heymoose().click(offerId, affId, sourceId, subs);
  }

  private static void doCreateGrant(long offerId, long affId) {
    long grantId = heymoose().createGrant(offerId, affId, "msg", baseUrl() + "/postback");
    heymoose().unblockGrant(grantId);
    heymoose().approveGrant(grantId);
  }

  @BeforeClass
  public static void beforeClass() {
    reset();

    sqlUpdate("insert into category(id, grouping, name) values(1, 'Group1', 'Category1')");
    sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");

    advertiserId = doRegisterAdvertiser();
    affId = doRegisterAffiliate();

    for (int i = 0; i < 4; i++) { // 4 offers
      // create offer
      Pair<Long, String> createdOfferPair = doCreateOffer(advertiserId, i);
      long offerId = (lastOfferId = createdOfferPair.fst);
      String offerCode = createdOfferPair.snd;
      doCreateGrant(offerId, affId);
      String sourceId = i + "-sourceId";
      Subs subs = new Subs(i + "-subId", null, i + "-subId2", null, i + "-subId4");

      // 3 shows
      for (int j = 0; j < 3; j++) {
        // show
        assertEquals(200, heymoose().track(offerId, affId, sourceId, subs));
      }

      // 1 more show with click
      {
        // show
        assertEquals(200, heymoose().track(offerId, affId, sourceId, subs));

        // click
        URI location = doClick(offerId, affId, sourceId, subs);
        assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
      }

      // 1 more show with click and action
      {
        // show
        assertEquals(200, heymoose().track(offerId, affId, sourceId, subs));

        // click
        URI location = doClick(offerId, affId, sourceId, subs);
        assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());

        // action
        String token = QueryUtil.extractParam(URLEncodedUtils.parse(location, "UTF-8"), "_hm_token");
        assertEquals(200, heymoose().action(token, "tx1", advertiserId, offerCode));
      }
    }

    // flush buffered data
    heymoose().flushBufferedCounters();
  }

  private OverallOfferStatsList stats;

  @Test
  public void getAffiliatesAllStatsGranted() {
    // 5 shows, 2 clicks
    stats = heymoose().getAffiliatesAllStats(true);
    assertEquals(1L, stats.count);
    assertNotNull(stats.stats);
    assertEquals(20L, stats.stats.get(0).shows);
    assertEquals(8L, stats.stats.get(0).clicks);
    assertEquals(4L, stats.stats.get(0).leads);
  }

  @Test
  public void getAffiliatesAllStatsNonGranted() {
    // 5 shows, 2 clicks
    stats = heymoose().getAffiliatesAllStats(false);
    assertEquals(1L, stats.count);
    assertNotNull(stats.stats);
    assertEquals(20L, stats.stats.get(0).shows);
    assertEquals(8L, stats.stats.get(0).clicks);
    assertEquals(4L, stats.stats.get(0).leads);
  }

  @Test
  public void getAdvertiserAllStats() {
    // 5 shows, 2 clicks
    stats = heymoose().getAdvertiserAllStats(false);
    assertEquals(1L, stats.count);
    assertNotNull(stats.stats);
    assertEquals(20L, stats.stats.get(0).shows);
    assertEquals(8L, stats.stats.get(0).clicks);
    assertEquals(4L, stats.stats.get(0).leads);
  }

  @Test
  public void getAdvertiserAllStatsExpired() {
    // 5 shows, 2 clicks
    stats = heymoose().getAdvertiserAllStats(true);
    assertEquals(0L, stats.count);
    assertNotNull(stats.stats);
  }

  @Test
  public void getAffiliatesStatsByOfferGranted() {
    // 5 shows, 2 clicks
    stats = heymoose().getAffiliatesStatsByOffer(true, lastOfferId);
    assertEquals(1L, stats.count);
    assertNotNull(stats.stats);
    assertEquals(5L, stats.stats.get(0).shows);
    assertEquals(2L, stats.stats.get(0).clicks);
    assertEquals(1L, stats.stats.get(0).leads);
  }

  @Test
  public void getAffiliatesStatsByOfferNonGranted() {
    // 5 shows, 2 clicks
    stats = heymoose().getAffiliatesStatsByOffer(false, lastOfferId);
    assertEquals(1L, stats.count);
    assertNotNull(stats.stats);
    assertEquals(5L, stats.stats.get(0).shows);
    assertEquals(2L, stats.stats.get(0).clicks);
    assertEquals(1L, stats.stats.get(0).leads);
  }

  @Test
  public void getOffersAllStatsGranted() {
    // only one is not null
    stats = heymoose().getOffersAllStats(true);
    assertEquals(4L, stats.count);
    assertNotNull(stats.stats);
    for (int i = 0; i < 4; i++) {
      assertEquals(5L, stats.stats.get(i).shows);
      assertEquals(2L, stats.stats.get(i).clicks);
      assertEquals(1L, stats.stats.get(i).leads);
    }
  }

  @Test
  public void getOffersAllStatsNotGranted() {
    // only one is not null
    stats = heymoose().getOffersAllStats(false);
    assertEquals(4L, stats.count);
    assertNotNull(stats.stats);
    for (int i = 0; i < 4; i++) {
      assertEquals(5L, stats.stats.get(i).shows);
      assertEquals(2L, stats.stats.get(i).clicks);
      assertEquals(1L, stats.stats.get(i).leads);
    }
  }

  @Test
  public void getOffersStatsByAdvertizerGranted() {
    // only one is not null
    stats = heymoose().getOffersStatsByAdvertizer(true, advertiserId);
    assertEquals(4L, stats.count);
    assertNotNull(stats.stats);
    for (int i = 0; i < 4; i++) {
      assertEquals(5L, stats.stats.get(i).shows);
      assertEquals(2L, stats.stats.get(i).clicks);
      assertEquals(1L, stats.stats.get(i).leads);
    }
  }

  @Test
  public void getOffersStatsByAdvertizerNonGranted() {
    // only one is not null
    stats = heymoose().getOffersStatsByAdvertizer(false, advertiserId);
    assertEquals(4L, stats.count);
    assertNotNull(stats.stats);
    for (int i = 0; i < 4; i++) {
      assertEquals(5L, stats.stats.get(i).shows);
      assertEquals(2L, stats.stats.get(i).clicks);
      assertEquals(1L, stats.stats.get(i).leads);
    }
  }

  @Test
  public void getOffersStatsByAffiliateGranted() {
    // only one is not null
    stats = heymoose().getOffersStatsByAffiliate(true, affId);
    assertEquals(4L, stats.count);
    assertNotNull(stats.stats);
    for (int i = 0; i < 4; i++) {
      assertEquals(5L, stats.stats.get(i).shows);
      assertEquals(2L, stats.stats.get(i).clicks);
      assertEquals(1L, stats.stats.get(i).leads);
    }
  }

  @Test
  public void getOffersStatsByAffiliateNonGranted() {
    // only one is not null
    stats = heymoose().getOffersStatsByAffiliate(false, affId);
    assertEquals(4L, stats.count);
    assertNotNull(stats.stats);
    for (int i = 0; i < 4; i++) {
      assertEquals(5L, stats.stats.get(i).shows);
      assertEquals(2L, stats.stats.get(i).clicks);
      assertEquals(1L, stats.stats.get(i).leads);
    }
  }

  @Test
  public void testApproveOffersWithHoldExpired() {
    heymoose().approveOfferWithHoldExpired(null);
    // mmm, no exceptions is a good sign

    heymoose().approveOfferWithHoldExpired(lastOfferId);
    // again mmm, no exceptions is a good sign
  }
}
