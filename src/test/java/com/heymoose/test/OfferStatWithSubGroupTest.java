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

    private static Long advertiserId = 0L;
    private static Long affId = 0L;
    private static Long lastOfferId = 0L;

    private static Subs sub_ = new Subs("2-sourceId", "2-subId", null, "2-subId2", null, "2-subId4", "sub_id2");

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

    private static URI doClick(long offerId, long affId, Subs subs) {
        return heymoose().click(offerId, affId, subs);
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

        advertiserId = doRegisterAdvertiser();
        affId = doRegisterAffiliate();

        for (int i = 0; i < 4; i++) { // 4 offers
            // create offer
            Pair<Long, String> createdOfferPair = doCreateOffer(advertiserId, i);
            long offerId = (lastOfferId = createdOfferPair.fst);
            String offerCode = createdOfferPair.snd;
            doCreateGrant(offerId, affId);
            int k = rnd.nextInt(3);
            Subs subs = new Subs(k + "-sourceId", k + "-subId", null, k + "-subId2", null, k + "-subId4");

            // 3 shows
            for (int j = 0; j < 3; j++) {
                // show
                assertEquals(200, heymoose().track(offerId, affId, subs));
            }

            // 1 more show with click
            {
                // show
                assertEquals(200, heymoose().track(offerId, affId, subs));

                // click
                URI location = doClick(offerId, affId, subs);
                assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
            }

            // 1 more show with click and action
            {
                // show
                assertEquals(200, heymoose().track(offerId, affId, subs));

                // click
                URI location = doClick(offerId, affId, subs);
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
    public void getAffiliatesAllStats() {
        stats = heymoose().getAffiliatesAllStats(sub_);
        assertNotNull(stats.stats);
    }

    @Test
    public void getAffiliatesStatsByOffer() {
        stats = heymoose().getAffiliatesStatsByOffer(lastOfferId, sub_);
        assertNotNull(stats.stats);
    }

    @Test
    public void getOffersAllStatsGranted() {
        stats = heymoose().getOffersAllStats(true, sub_);
        assertNotNull(stats.stats);
    }

    @Test
    public void getOffersAllStatsNotGranted() {
        stats = heymoose().getOffersAllStats(false, sub_);
        assertNotNull(stats.stats);
    }

    @Test
    public void getOffersStatsByAdvertizer() {
        stats = heymoose().getOffersStatsByAdvertizer(advertiserId, sub_);
        assertNotNull(stats.stats);
    }

    @Test
    public void getOffersStatsByAffiliate() {
        stats = heymoose().getOffersStatsByAffiliate(affId, sub_);
        assertNotNull(stats.stats);
    }
}
