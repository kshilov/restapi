package com.heymoose.test;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.user.Role;
import com.heymoose.infrastructure.util.Paging;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.resource.xml.XmlWithdraws;
import com.heymoose.test.base.RestTest;
import org.joda.time.DateTimeUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.Random;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

public class WithdrawTest extends RestTest {

  private final static String OFFER_CODE = "offer324234";
  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_SITE_URL = "http://yandex.ru";
  private final static String OFFER_NAME = "Offer1";
  private final static double ADV_BALANCE = 1000.0;
  private final static double OFFER_BALANCE = 10.0;
  private final static double CPA = 30.0;
  private final static String AFF_EMAIL = "af1@af.ru";

  private static final Random rnd = new Random(System.currentTimeMillis());

  private static Long advertiserId = 0L;
  private static Long advAccountId = 0L;
  private static Long affId = 0L;
  private static Long affAccountId = 0L;
  private static Long lastWithdrawId = 0L;

  private static long doRegisterAdvertiser() {
    long advertiserId = heymoose().registerUser("u@u.ru", "ads");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, ADV_BALANCE);
    return advertiserId;
  }

  private static long doRegisterAffiliate() {
    long affId = heymoose().registerUser(AFF_EMAIL, "dsfs");
    heymoose().addRoleToUser(affId, Role.AFFILIATE);
    heymoose().confirmUser(affId);
    return affId;
  }

  private static Pair<Long, String> doCreateOffer(long advertiserId, int seed) {
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    String offerCode = OFFER_CODE + "-" + seed + "-" + rnd.nextInt(1000);
    long offerId = heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, CPA, null, OFFER_BALANCE,
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

    // flush buffered data
    heymoose().flushBufferedCounters();
  }

  @Test
  public void getAllWithdrawStats() {
    advertiserId = doRegisterAdvertiser();
    advAccountId = heymoose().getUser(advertiserId).advertiserAccount.id;
    affId = doRegisterAffiliate();
    affAccountId = heymoose().getUser(affId).affiliateAccount.id;

    // 1st
    heymoose().transfer(advAccountId, affAccountId, 20.0);
    lastWithdrawId = heymoose().createWithdraw(affAccountId);

    // 2nd, approved
    heymoose().transfer(advAccountId, affAccountId, 10.0);
    lastWithdrawId = heymoose().createWithdraw(affAccountId);
    heymoose().approveWithdraw(lastWithdrawId);

    XmlWithdraws affWithdraws = heymoose().getWithdrawByAff(affId);
    assertNotNull(affWithdraws);
    assertNull(affWithdraws.count);
    assertEquals(affAccountId, affWithdraws.accountId);
    assertNull(affWithdraws.nonApprovedCount);
    assertNull(affWithdraws.nonApprovedTotal);
    //
    assertTrue(affWithdraws.withdraws.get(0).done);
    assertFalse(affWithdraws.withdraws.get(1).done);
    assertEquals(10d, affWithdraws.withdraws.get(0).amount, 0.001d);
    assertEquals(20d, affWithdraws.withdraws.get(1).amount, 0.001d);
    assertNull(affWithdraws.withdraws.get(0).affEmail);
    assertNull(affWithdraws.withdraws.get(1).affEmail);
    assertNull(affWithdraws.withdraws.get(0).affId);
    assertNull(affWithdraws.withdraws.get(1).affId);

    XmlWithdraws allWithdrawStats = heymoose().getAllWithdrawStats(new Paging(0, 10));
    assertNotNull(allWithdrawStats);
    assertTrue(2L == allWithdrawStats.count);
    assertNull(allWithdrawStats.accountId);
    assertTrue(1L == allWithdrawStats.nonApprovedCount);
    assertEquals(20d, allWithdrawStats.nonApprovedTotal, 0.001d);
    //
    assertTrue(allWithdrawStats.withdraws.get(0).done);
    assertFalse(allWithdrawStats.withdraws.get(1).done);
    assertEquals(10d, allWithdrawStats.withdraws.get(0).amount, 0.001d);
    assertEquals(20d, allWithdrawStats.withdraws.get(1).amount, 0.001d);
    assertEquals(AFF_EMAIL, allWithdrawStats.withdraws.get(0).affEmail);
    assertEquals(AFF_EMAIL, allWithdrawStats.withdraws.get(1).affEmail);
    assertEquals(affId, allWithdrawStats.withdraws.get(0).affId);
    assertEquals(affId, allWithdrawStats.withdraws.get(1).affId);
  }


}
