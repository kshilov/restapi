package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.resource.xml.XmlUser;
import com.heymoose.test.base.RestTest;
import com.heymoose.util.NameValuePair;
import com.heymoose.util.URLEncodedUtils;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Random;
import org.joda.time.DateTimeUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;

public class OfferStatTest extends RestTest {

    private final static String OFFER_CODE = "offer324234";
    private final static String OFFER_URL = "http://ya.ru";
    private final static String OFFER_SITE_URL = "http://yandex.ru";
    private final static String OFFER_NAME = "Offer1";
    private final static double ADV_BALANCE = 1000.0;
    private final static double OFFER_BALANCE = 10.0;
    private final static double CPA = 30.0;

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

    private static final Random rnd = new Random(System.currentTimeMillis());

    private long doCreateOffer(long advertiserId, int seed) {
        long categoryId = heymoose().getCategories().categories.iterator().next().id;
        long offerId = heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, CPA, OFFER_BALANCE,
            OFFER_NAME, "descr", "short descr", "logo", URI.create(OFFER_URL), URI.create(OFFER_SITE_URL),
            "title", false, false, true, newHashSet(Region.RUSSIA), newHashSet(categoryId),
            OFFER_CODE + "-" + seed + "-" + rnd.nextInt(1000), 30, 180, DateTimeUtils.currentTimeMillis());
        heymoose().approveOffer(offerId);
        return offerId;
    }

    private URI doClick(long offerId, long affId, Subs subs) {
        URI location = heymoose().click(offerId, affId, subs);
        return location;
    }

    private void doCreateGrant(long offerId, long affId) {
        long grantId = heymoose().createGrant(offerId, affId, "msg", baseUrl() + "/postback");
        heymoose().unblockGrant(grantId);
        heymoose().approveGrant(grantId);
    }

    @Test
    public void affStat() {
        sqlUpdate("insert into category(id, grouping, name) values(1, 'Group1', 'Category1')");
        sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");

        long advertiserId = doRegisterAdvertiser();
        long affId = doRegisterAffiliate();
        Long lastOfferId = 0L;

        for (int i = 0; i < 4; i++) { // 4 offers
            // create offer
            long offerId = (lastOfferId = doCreateOffer(advertiserId, i));
            doCreateGrant(offerId, affId);
            Subs subs = new Subs(i + "-sourceId", i + "-subId", null, i + "-subId2", null, i + "-subId4");

            // 3 shows
            for (int j = 0; j < 3; j++) {
                // show
                assertEquals(200, heymoose().track(offerId, affId, subs));
            }

            // 2 more shows with clicks
            for (int j = 0; j < 2; j++) {
                // show
                assertEquals(200, heymoose().track(offerId, affId, subs));

                // click
                URI location = doClick(offerId, affId, subs);
                assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
            }

        }

        // flush buffered data
        heymoose().flushBufferedCounters();

        // stats for the last
        List<Subs> subs = asList(
            new Subs("3-sourceId", "3-subId", null, "3-subId2", null, "3-subId4"),
            new Subs("3-sourceId", "3-subId", null, "3-subId2", null, null),
            new Subs("3-sourceId", "3-subId", null, null, null, null),
            new Subs("3-sourceId", null, null, null, null, null)
        );

        OverallOfferStatsList stats;

        // 5 shows, 2 clicks
        for (Subs sub : subs) {
            stats = heymoose().getAffiliatesAllStats(sub);
            assertEquals(stats.count, 1L);
            assertEquals(stats.stats.get(0).shows, 5L);
            assertEquals(stats.stats.get(0).clicks, 2L);
        }
        for (Subs sub : subs) {
            stats = heymoose().getAffiliatesStatsByOffer(lastOfferId, sub);
            assertEquals(stats.count, 1L);
            assertEquals(stats.stats.get(0).shows, 5L);
            assertEquals(stats.stats.get(0).clicks, 2L);
        }

        // not related offerId and subs
        Subs sub_ = new Subs("2-sourceId", "2-subId", null, "2-subId2", null, "2-subId4");
        stats = heymoose().getAffiliatesStatsByOffer(lastOfferId, sub_);
        assertEquals(stats.count, 1L);
        assertEquals(stats.stats.get(0).shows, 0L);
        assertEquals(stats.stats.get(0).clicks, 0L);

        // nothing in return
        for (Subs sub : subs) {
            stats = heymoose().getAffiliatesStatsByOffer(0L, sub);
            assertEquals(stats.count, 0L);
            assertNotNull(stats.stats);
            assertEquals(stats.stats.size(), 0);
        }
    }

    @Test
    @Ignore
    public void actionWithSubs() {
        long advertiserId = doRegisterAdvertiser();
        long offerId = doCreateOffer(advertiserId, 1);
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
