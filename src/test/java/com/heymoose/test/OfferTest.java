package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.test.base.RestTest;
import java.net.URI;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class OfferTest extends RestTest {

  private final static String OFFER_URL = "http://ya.ru";
  private final static String OFFER_NAME = "Offer1";

  private long doCreateOffer() {
    sqlUpdate("insert into category(id, grouping, name) values(1, 'Group1', 'Category1')");
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    long advertiserId = heymoose().registerUser("u@u.ru", "ads", "F", "L", "777");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, 100.0);
    return heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, 30.0, 60.0,
        OFFER_NAME, "descr", "logo", URI.create(OFFER_URL), "title", false, false,
        true, newHashSet(Region.RUSSIA), newHashSet(categoryId));
  }

  @Test public void createOffer() {
    long offerId = doCreateOffer();
    XmlOffer offer = heymoose().getOffer(offerId);
    assertEquals(OFFER_NAME, offer.name);
  }

  @Test public void track() {
    long offerId = doCreateOffer();
    long affId = heymoose().registerUser("af1@af.ru", "dsfs", "F", "L", "777");
    heymoose().addRoleToUser(affId, Role.AFFILIATE);
    heymoose().confirmUser(affId);
    heymoose().approveOffer(offerId);
    long grantId = heymoose().createGrant(offerId, affId, "msg");
    heymoose().unblockGrant(grantId);
    heymoose().approveGrant(grantId);
    heymoose().track(offerId, affId);
  }

  @Test public void click() {
    sqlUpdate("insert into ip_segment(id, start_ip_addr, end_ip_addr, start_ip_num, end_ip_num, country_code, country_name) values(1, '127.0.0.1', '127.0.0.1', 2130706433, 2130706433, 'RU', 'Russian')");
    long offerId = doCreateOffer();
    long affId = heymoose().registerUser("af1@af.ru", "dsfs", "F", "L", "777");
    heymoose().addRoleToUser(affId, Role.AFFILIATE);
    heymoose().confirmUser(affId);
    heymoose().approveOffer(offerId);
    long grantId = heymoose().createGrant(offerId, affId, "msg");
    heymoose().unblockGrant(grantId);
    heymoose().approveGrant(grantId);
    URI location = heymoose().click(offerId, affId);
    assertEquals(URI.create(OFFER_URL).getHost(), location.getHost());
  }
}
