package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.test.base.RestTest;
import com.heymoose.util.Pair;
import com.heymoose.util.QueryUtil;
import com.heymoose.util.URLEncodedUtils;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.Random;
import org.joda.time.DateTimeUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

public class OfferStatWithSubGroupTest extends RestTest {

  private final static String OFFER_CODE = "offer324234";
  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_SITE_URL = "http://yandex.ru";
  private final static String OFFER_NAME = "Offer1";
  private final static double ADV_BALANCE = 1000.0;
  private final static double OFFER_BALANCE = 10.0;
  private final static double CPA = 30.0;

  private static final Random rnd = new Random(System.currentTimeMillis());

  private static Long advertiserId = -1L;
  private static Long affId = -1L;
  private static Long lastOfferId = -1L;

  private static long doRegisterAdvertiser() {
    long advertiserId = heymoose().registerUser("u@u.ru", "ads", "F", "L", "777");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, ADV_BALANCE);
    return advertiserId;
  }

  private static long doRegisterAffiliate() {
    long affId = heymoose().registerUser("af1@af.ru", "dsfs", "F", "L", "777");
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
        URI location = doClick(offerId, affId, sourceId, subs);
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
  public void getSourceIdStatsGranted() {
    stats = heymoose().getSourceIdStats(true, null, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsGrantedWithOffer() {
    stats = heymoose().getSourceIdStats(true, null, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsNonGranted() {
    stats = heymoose().getSourceIdStats(false, null, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsNonGrantedWithOffer() {
    stats = heymoose().getSourceIdStats(false, null, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAffGranted() {
    stats = heymoose().getSourceIdStats(true, affId, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAffGrantedWithOffer() {
    stats = heymoose().getSourceIdStats(true, affId, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAffNonGranted() {
    stats = heymoose().getSourceIdStats(false, affId, null);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAffNonGrantedWithOffer() {
    stats = heymoose().getSourceIdStats(false, affId, lastOfferId);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAdvGranted() {
    stats = heymoose().getSourceIdStats(true, advertiserId, null);
    assertEquals(0L, stats.count);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAdvGrantedWithOffer() {
    stats = heymoose().getSourceIdStats(true, advertiserId, lastOfferId);
    assertEquals(0L, stats.count);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAdvNonGranted() {
    stats = heymoose().getSourceIdStats(false, advertiserId, null);
    assertEquals(0L, stats.count);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSourceIdStatsForAdvNonGrantedWithOffer() {
    stats = heymoose().getSourceIdStats(false, advertiserId, lastOfferId);
    assertEquals(0L, stats.count);
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGranted() {
    stats = heymoose().getSubIdStats(
        true, null, null,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedWithOffer() {
    stats = heymoose().getSubIdStats(
        true, null, lastOfferId,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGranted() {
    stats = heymoose().getSubIdStats(
        true, affId, null,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedWithOffer() {
    stats = heymoose().getSubIdStats(
        true, affId, lastOfferId,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedPartialGroup() {
    stats = heymoose().getSubIdStats(
        true, null, null,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        true, null, lastOfferId,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedPartialGroup() {
    stats = heymoose().getSubIdStats(
        true, affId, null,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        true, affId, lastOfferId,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedFilter() {
    stats = heymoose().getSubIdStats(
        true, null, null,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedFilterWithOffer() {
    stats = heymoose().getSubIdStats(
        true, null, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedFilter() {
    stats = heymoose().getSubIdStats(
        true, affId, null,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedFilterWithOffer() {
    stats = heymoose().getSubIdStats(
        true, affId, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedFilterAndPartialGroup() {
    stats = heymoose().getSubIdStats(
        true, null, null,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsGrantedFilterAndPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        true, null, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedFilterAndPartialGroup() {
    stats = heymoose().getSubIdStats(
        true, affId, null,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffGrantedFilterAndPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        true, affId, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGranted() {
    stats = heymoose().getSubIdStats(
        false, null, null,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedWithOffer() {
    stats = heymoose().getSubIdStats(
        false, null, lastOfferId,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGranted() {
    stats = heymoose().getSubIdStats(
        false, affId, null,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedWithOffer() {
    stats = heymoose().getSubIdStats(
        false, affId, lastOfferId,
        Subs.empty(), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedPartialGroup() {
    stats = heymoose().getSubIdStats(
        false, null, null,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(false, null, lastOfferId, Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedPartialGroup() {
    stats = heymoose().getSubIdStats(
        false, affId, null,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        false, affId, lastOfferId,
        Subs.empty(), asList(true, true, true, false, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedFilter() {
    stats = heymoose().getSubIdStats(
        false, null, null,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedFilterWithOffer() {
    stats = heymoose().getSubIdStats(
        false, null, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedFilter() {
    stats = heymoose().getSubIdStats(
        false, affId, null,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedFilterWithOffer() {
    stats = heymoose().getSubIdStats(
        false, affId, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(true, true, true, true, true));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedFilterAndPartialGroup() {
    stats = heymoose().getSubIdStats(
        false, null, null,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsNonGrantedFilterAndPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        false, null, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedFilterAndPartialGroup() {
    stats = heymoose().getSubIdStats(
        false, affId, null,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }

  @Test
  public void getSubIdStatsAffNonGrantedFilterAndPartialGroupWithOffer() {
    stats = heymoose().getSubIdStats(
        false, affId, lastOfferId,
        new Subs(null, null, "2-subId2", null, null), asList(false, true, true, true, false));
    assertNotNull(stats.stats);
  }
}
