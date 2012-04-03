package com.heymoose.test;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Role;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.test.base.RestTest;
import java.net.URI;
import org.junit.Test;

public class OfferTest extends RestTest {

  @Test public void createOffer() {
    sqlUpdate("insert into category(id, grouping, name) values(1, 'Group1', 'Category1')");
    long categoryId = heymoose().getCategories().categories.iterator().next().id;
    long advertiserId = heymoose().registerUser("u@u.ru", "ads", "F", "L", "777");
    heymoose().addRoleToUser(advertiserId, Role.ADVERTISER);
    heymoose().confirmUser(advertiserId);
    heymoose().addToCustomerAccount(advertiserId, 100.0);
    heymoose().createOffer(advertiserId, PayMethod.CPA, CpaPolicy.FIXED, 30.0, 60.0,
        "Offer1", "descr", "logo", URI.create("http://ya.ru"), "title", false, false,
        true, newHashSet(Region.RUSSIA), newHashSet(categoryId));
  }
}
