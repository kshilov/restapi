package com.heymoose.domain.affiliate.hiber;

import static com.google.common.collect.Sets.newHashSet;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.Offer;
import com.heymoose.domain.affiliate.Site;
import org.hibernate.Session;

@Singleton
public class OfferFinder {
  
  private final Provider<Session> sessionProvider;

  @Inject
  public OfferFinder(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  private Session hiber() {
    return sessionProvider.get();
  }
  
  public List<Offer> findOffers(Site site) {
    String sql = "select offer_id " +
        "from offer " +
        "where " +
        "and (select allow_negative_balance = true or cpa <= balance from account where id = account_id) " +
        "and approved = true and active = true " +
        "and offer.type = 3 " +
        "and :categories in (select category_id from offer_category oc where oc.offer_id = offer.id) " +
        "and :regions in (select region from offer_region or where or.offer_id = offer.id)";

    Set<Long> categories = newHashSet();
    for (Category category : site.categories())
      categories.add(category.id());

    @SuppressWarnings("unchecked")
    List<Object[]> list = hiber()
        .createSQLQuery(sql)
        .setParameterList("categories", categories)
        .setParameterList("regions", site.regions())
        .list();
    
    Set<Long> offerIds = newHashSet();
    for (Object[] record : list)
      offerIds.add(((BigInteger) record[0]).longValue());
    
    return hiber()
        .createQuery("from Offer as offer inner join fetch offer.order where offer.id in :ids")
        .setParameterList("ids",offerIds)
        .list();
  }
}
