package com.heymoose.domain.affiliate.hiber;

import com.heymoose.domain.affiliate.Offer;
import com.heymoose.domain.affiliate.OfferRepository;
import com.heymoose.domain.affiliate.repository.OfferFilter;
import com.heymoose.domain.hiber.RepositoryHiber;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class OfferRepositoryHiber extends RepositoryHiber<Offer> implements OfferRepository {
  
  @Inject
  public OfferRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterable<Offer> list(Ordering ord, boolean asc, int offset, int limit,
                              OfferFilter filter) {
    Criteria criteria = hiber().createCriteria(getEntityClass());

    fillCriteriaFromFilter(criteria, filter);
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long count(OfferFilter filter) {
    Criteria criteria = hiber().createCriteria(getEntityClass());

    fillCriteriaFromFilter(criteria, filter);

    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }


  @Override
  public Iterable<Offer> list(Ordering ord, boolean asc, int offset, int limit,
                              Boolean approved, Boolean active, Boolean launched, Boolean showcase,
                              Long advertiserId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (advertiserId != null)
      criteria.add(Restrictions.eq("advertiser.id", advertiserId));
    if (approved != null)
      criteria.add(Restrictions.eq("approved", approved));
    if (active != null)
      criteria.add(Restrictions.eq("active", active));
    if (launched != null)
      criteria.add(Restrictions.lt("launchTime", DateTime.now()));
    if (showcase != null)
      criteria.add(Restrictions.eq("showcase", showcase));
    
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long count(Boolean approved, Boolean active, Boolean launched, Boolean showcase, Long advertiserId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (advertiserId != null)
      criteria.add(Restrictions.eq("advertiser.id", advertiserId));
    if (approved != null)
      criteria.add(Restrictions.eq("approved", approved));
    if (active != null)
      criteria.add(Restrictions.eq("active", active));
    if (launched != null)
      criteria.add(Restrictions.lt("launchTime", DateTime.now()));
    if (showcase != null)
      criteria.add(Restrictions.eq("showcase", showcase));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }
  
  @Override
  public Iterable<Offer> listRequested(Ordering ord, boolean asc, int offset, int limit,
                                          long affiliateId, Boolean active) {
    Criteria criteria = hiber()
        .createCriteria(getEntityClass())
        .createAlias("grants", "grants")
        .add(Restrictions.eq("grants.affiliate.id", affiliateId));
    
    if (active != null)
      criteria.add(Restrictions.eq("grants.active", active));
    
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long countRequested(long affiliateId, Boolean active) {
    Criteria criteria = hiber()
        .createCriteria(getEntityClass())
        .createAlias("grants", "grants")
        .add(Restrictions.eq("grants.affiliate.id", affiliateId));
    
    if (active != null)
      criteria.add(Restrictions.eq("grants.active", active));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  @Override
  protected Class<Offer> getEntityClass() {
    return Offer.class;
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

  private static Criteria addRestrictionIfNotNull(Criteria criteria,
                                                  String paramName,
                                                  Object value) {
    if (value != null) {
      return criteria.add(Restrictions.eq(paramName, value));
    }
    return criteria;
  }

  private static Criteria fillCriteriaFromFilter(Criteria criteria,
                                                 OfferFilter filter) {
    addRestrictionIfNotNull(criteria, "advertiser.id", filter.advertiserId());
    addRestrictionIfNotNull(criteria, "approved", filter.approved());
    addRestrictionIfNotNull(criteria, "active", filter.active());
    addRestrictionIfNotNull(criteria, "showcase", filter.showcase());
    if (filter.launched() != null)
      criteria.add(Restrictions.lt("launchTime", DateTime.now()));
    return criteria;

  }
}
