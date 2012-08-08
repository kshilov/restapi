package com.heymoose.infrastructure.persistence;

import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferFilter;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.PayMethod;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.heymoose.infrastructure.util.HibernateUtil.*;

@Singleton
public class OfferRepositoryHiber extends RepositoryHiber<Offer> implements
    OfferRepository {
  
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

  private static Criteria fillCriteriaFromFilter(Criteria criteria,
                                                 OfferFilter filter) {
    addEqRestrictionIfNotNull(criteria, "advertiser.id", filter.advertiserId());
    addEqRestrictionIfNotNull(criteria, "approved", filter.approved());
    addEqRestrictionIfNotNull(criteria, "active", filter.active());
    addEqRestrictionIfNotNull(criteria, "showcase", filter.showcase());

    if (filter.launched() != null && filter.launched())
      criteria.add(Restrictions.lt("launchTime", DateTime.now()));

    if (filter.payMethod() == PayMethod.CPA) {
      Criterion parentPayMethodMatches = Restrictions.and(
          Restrictions.eq("payMethod", filter.payMethod()),
          Restrictions.eq("cpaPolicy", filter.cpaPolicy()));
      Criterion subPayMethodMatches =
          Restrictions.sqlRestriction(
              "exists (select * from offer " +
                  "where parent_id = {alias}.id " +
                  "and pay_method = ? " +
                  "and cpa_policy = ?)",
              new String[] {
                  filter.payMethod().toString(),
                  filter.cpaPolicy().toString() },
              new Type[] {
                  StandardBasicTypes.STRING,
                  StandardBasicTypes.STRING });
      criteria.add(Restrictions.or(parentPayMethodMatches, subPayMethodMatches));
    }

    if (filter.payMethod() == PayMethod.CPC) {
      Criterion parentPayMethodMatches =
          Restrictions.eq("payMethod", filter.payMethod());
      Criterion subPayMethodMatches =
          Restrictions.sqlRestriction(
              "exists (select * from offer " +
                  "where parent_id = {alias}.id " +
                  "and pay_method = ? )",
              filter.payMethod().toString(),
              StandardBasicTypes.STRING);
      criteria.add(Restrictions.or(parentPayMethodMatches, subPayMethodMatches));
    }

    String existsRegion = "exists (select * from offer_region r " +
            "where {alias}.id = r.offer_id and region in (?))";
    addSqlInRestriction(criteria, existsRegion, filter.regionList(),
        StandardBasicTypes.STRING);

    String existsCategory = "exists (select * from offer_category c " +
            "where {alias}.id = c.offer_id and category_id in (?))";
    addSqlInRestriction(criteria, existsCategory, filter.categoryIdList(),
        StandardBasicTypes.LONG);

    return criteria;

  }

}
