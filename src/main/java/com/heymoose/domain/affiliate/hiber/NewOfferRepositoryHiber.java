package com.heymoose.domain.affiliate.hiber;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.heymoose.domain.affiliate.NewOffer;
import com.heymoose.domain.affiliate.NewOfferRepository;
import com.heymoose.domain.hiber.RepositoryHiber;

@Singleton
public class NewOfferRepositoryHiber extends RepositoryHiber<NewOffer> implements NewOfferRepository {
  
  @Inject
  public NewOfferRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Iterable<NewOffer> list(Ordering ord, boolean asc, int offset, int limit, Long advertiserId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (advertiserId != null)
      criteria.add(Restrictions.eq("advertiser.id", advertiserId));
    
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long count(Long advertiserId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (advertiserId != null)
      criteria.add(Restrictions.eq("advertiser.id", advertiserId));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  @Override
  protected Class<NewOffer> getEntityClass() {
    return NewOffer.class;
  }
  
  private static void setOrdering(Criteria criteria, Ordering ord, boolean asc) {
    switch (ord) {
    case ID: criteria.addOrder(order("id", asc)); break;
    case NAME: criteria.addOrder(order("name", asc)); break;
    case URL: criteria.addOrder(order("url", asc)); break;
    case ADVERTISER_LAST_NAME: criteria
      .createAlias("advertiser", "advertiser")
      .addOrder(order("advertiser.lastName", asc)); break;
    }
    
    if (ord != Ordering.ID)
      criteria.addOrder(order("id", asc));
  }
}
