package com.heymoose.infrastructure.persistence;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferFilter;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.TypedMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
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
import java.util.List;
import java.util.Map;

import static com.heymoose.infrastructure.util.db.HibernateUtil.*;

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
  @SuppressWarnings("unchecked")
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
  public Pair<List<Offer>, Long> affiliateOfferList(long affId,
                                                    int offset, int limit) {
    Pair<QueryResult, Long> idResult = SqlLoader
        .sqlQuery("affiliate-offer-id-list", hiber())
        .addQueryParam("aff_id", affId)
        .executeAndCount(offset, limit);
    ImmutableList.Builder<Offer> offerList = ImmutableList.builder();
    for (Map<String, Object> idMap : idResult.fst) {
      TypedMap map = TypedMap.wrap(idMap);
      offerList.add((Offer) hiber().get(Offer.class, map.getLong("id")));
    }
    return Pair.of((List<Offer>) offerList.build(), idResult.snd);
  }

  @Override
  public Offer get(long offerId) {
    return (Offer) hiber().get(Offer.class, offerId);
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
    addEqRestrictionIfNotNull(criteria, "exclusive", filter.exclusive());

    if (filter.launched() != null && filter.launched())
      criteria.add(Restrictions.lt("launchTime", DateTime.now()));

    if (filter.affiliateId() != null) {
      criteria.add(Restrictions.sqlRestriction(
          "exists (select * from placement " +
              "join site on site.id = placement.site_id " +
              "where offer_id = {alias}.id " +
              "and site.aff_id = ?)",
          filter.affiliateId(),
          StandardBasicTypes.LONG));
    }

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
