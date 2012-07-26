package com.heymoose.infrastructure.persistence;

import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferFilter;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.SubOffer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;

import static com.heymoose.infrastructure.util.HibernateUtil.addEqRestrictionIfNotNull;

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


  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
  public Iterable<SubOffer> subOffers(int offset, int limit, Long parentOfferId) {
    return subOffers(offset, limit, parentOfferId, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterable<SubOffer> subOffers(int offset, int limit, Long parentOfferId,
                                      boolean activeOnly) {
    Criteria criteria = hiber().createCriteria(SubOffer.class)
        .add(Restrictions.eq("parentId", parentOfferId))
        .addOrder(Order.desc("percent"))
        .addOrder(Order.desc("cost"))
        .addOrder(Order.asc("id"))
        .setFirstResult(offset)
        .setMaxResults(limit);
    if (activeOnly)
      criteria.add(Restrictions.eq("active", true));
    return (List<SubOffer>) criteria.list();
  }

  public Long countSubOffers(Long parentOfferId) {
    return countSubOffers(parentOfferId, true);
  }

  @Override
  public Long countSubOffers(Long parentOfferId, boolean activeOnly) {
    Criteria criteria = hiber().createCriteria(SubOffer.class)
        .add(Restrictions.eq("parentId", parentOfferId))
        .setProjection(Projections.count("id"));
    if (activeOnly)
      criteria.add(Restrictions.eq("active", true));
    return (Long) criteria.uniqueResult();
  }

  public SubOffer subOfferByIdAndParentId(Long id, Long parentId) {
    return (SubOffer) hiber()
        .createQuery("from SubOffer where id = :id and parentId = :parentId")
        .setParameter("id", id)
        .setParameter("parentId", parentId)
        .uniqueResult();
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

    for (String region : filter.regionList()) {
      criteria.add(Restrictions.sqlRestriction(
          "exists (select * from offer_region r " +
              "where {alias}.id = r.offer_id and region = ?)",
          region, StandardBasicTypes.STRING));
    }

    for (Long category : filter.categoryIdList()) {
      criteria.add(Restrictions.sqlRestriction(
          "exists (select * from offer_category c " +
              "where {alias}.id = c.offer_id and category_id = ?)",
          category, StandardBasicTypes.LONG));
    }
    return criteria;

  }
}
