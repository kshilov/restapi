package com.heymoose.domain.affiliate;

import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Offer;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
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
        "join offer_order ord on ord.offer_id = offer.id " +
        "where " +
        "and (select allow_negative_balance = true or ord.cpa <= balance from account where id = ord.account_id) " +
        "and ord.disabled = false and (ord.paused is null or ord.paused = false) " +
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
