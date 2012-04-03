package com.heymoose.domain.affiliate.hiber;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.affiliate.NewOfferRepository.Ordering;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.domain.affiliate.OfferGrantState;
import com.heymoose.domain.hiber.RepositoryHiber;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class OfferGrantRepositoryHiber extends RepositoryHiber<OfferGrant> implements OfferGrantRepository {

  @Inject
  public OfferGrantRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }
  
  @Override
  protected Class<OfferGrant> getEntityClass() {
    return OfferGrant.class;
  }
  
  @Override
  public OfferGrant byOfferAndAffiliate(long offerId, long affiliateId) {
    return (OfferGrant) hiber().createCriteria(getEntityClass())
        .add(Restrictions.eq("offer.id", offerId))
        .add(Restrictions.eq("affiliate.id", affiliateId))
        .uniqueResult();
  }
  
  @Override
  public Map<Long, OfferGrant> byOffersAndAffiliate(Iterable<Long> offerIds, long affiliateId) {
    Iterable<OfferGrant> grants = (Iterable<OfferGrant>) hiber()
        .createCriteria(getEntityClass())
        .add(Restrictions.in("offer.id", newArrayList(offerIds)))
        .add(Restrictions.eq("affiliate.id", affiliateId))
        .list();
    
    Map<Long, OfferGrant> grantsMap = newHashMap();
    for (OfferGrant grant : grants)
      grantsMap.put(grant.offerId(), grant);
    return grantsMap;
  }

  @Override
  public Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                                 Long offerId, Long affiliateId, OfferGrantState state, Boolean blocked) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (affiliateId != null)
      criteria.add(Restrictions.eq("affiliate.id", affiliateId));
    if (state != null)
      criteria.add(Restrictions.eq("state", state.ordinal()));
    if (blocked != null)
      criteria.add(Restrictions.eq("blocked", blocked));
    
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long count(Long offerId, Long affiliateId, OfferGrantState state, Boolean blocked) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (affiliateId != null)
      criteria.add(Restrictions.eq("affiliate.id", affiliateId));
    if (state != null)
      criteria.add(Restrictions.eq("state", state.ordinal()));
    if (blocked != null)
      criteria.add(Restrictions.eq("blocked", blocked));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  private static void setOrdering(Criteria criteria, Ordering ord, boolean asc) {
    switch (ord) {
    case GRANT_ID: criteria.addOrder(order("id", asc)); break;
    case GRANT_APPROVED: criteria.addOrder(order("approved", asc)); break;
    case GRANT_ACTIVE: criteria.addOrder(order("active", asc)); break;
    case NAME: criteria
      .createAlias("offer", "offer")
      .addOrder(order("offer.name", asc));
    break;
    case GRANT_AFFILIATE_LAST_NAME: criteria
      .createAlias("affiliate", "affiliate")
      .addOrder(order("affiliate.lastName", asc));
    break;
    }
    
    if (ord != Ordering.ID)
      criteria.addOrder(order("id", asc));
  }
  
}
