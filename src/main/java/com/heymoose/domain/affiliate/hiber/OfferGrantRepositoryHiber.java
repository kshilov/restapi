package com.heymoose.domain.affiliate.hiber;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.domain.hiber.RepositoryHiber;

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
  public Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                                 Long offerId, Long affiliateId, Boolean approved, Boolean active) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (affiliateId != null)
      criteria.add(Restrictions.eq("affiliate.id", affiliateId));
    if (approved != null)
      criteria.add(Restrictions.eq("approved", approved));
    if (active != null)
      criteria.add(Restrictions.eq("active", active));
    
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long count(Long offerId, Long affiliateId, Boolean approved, Boolean active) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (affiliateId != null)
      criteria.add(Restrictions.eq("affiliate.id", affiliateId));
    if (approved != null)
      criteria.add(Restrictions.eq("approved", approved));
    if (active != null)
      criteria.add(Restrictions.eq("active", active));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  private static void setOrdering(Criteria criteria, Ordering ord, boolean asc) {
    switch (ord) {
    case ID: criteria.addOrder(order("id", asc)); break;
    case APPROVED: criteria.addOrder(order("approved", asc)); break;
    case ACTIVE: criteria.addOrder(order("active", asc)); break;
    case OFFER_NAME: criteria
      .createAlias("offer", "offer")
      .addOrder(order("offer.name", asc));
    break;
    case AFFILIATE_LAST_NAME: criteria
      .createAlias("affiliate", "affiliate")
      .addOrder(order("affiliate.lastName", asc)); break;
    }
    
    if (ord != Ordering.ID)
      criteria.addOrder(order("id", asc));
  }
  
}
