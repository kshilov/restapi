package com.heymoose.domain.affiliate.hiber;

import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.BaseOffer;
import com.heymoose.domain.affiliate.Offer;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.domain.affiliate.OfferGrantState;
import com.heymoose.domain.affiliate.OfferRepository.Ordering;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.domain.affiliate.repository.OfferGrantFilter;
import com.heymoose.domain.hiber.RepositoryHiber;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.heymoose.util.HibernateUtil.addEqRestrictionIfNotNull;

public class OfferGrantRepositoryHiber extends RepositoryHiber<OfferGrant> implements OfferGrantRepository {

  private final Repo repo;

  @Inject
  public OfferGrantRepositoryHiber(Provider<Session> sessionProvider, Repo repo) {
    super(sessionProvider);
    this.repo = repo;
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
  public OfferGrant visibleByOfferAndAff(BaseOffer offer, User affiliate) {
    BaseOffer grantTarget;
    if (offer instanceof Offer)
      grantTarget = offer;
    else if (offer instanceof SubOffer)
      grantTarget = ((SubOffer) offer).parent();
    else
      throw new IllegalStateException();
    OfferGrant grant = repo.byHQL(
        OfferGrant.class,
        "from OfferGrant where offer = ? and affiliate = ?",
        grantTarget, affiliate
    );
    if (grant == null)
      return null;
    if (!grant.offerIsVisible())
      return null;
    return grant;
  }

  @Override
  public Iterable<OfferGrant> list(Ordering ord, boolean asc, int offset, int limit,
                                 Long offerId, Long affiliateId, OfferGrantState state,
                                 Boolean blocked, Boolean moderation) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (affiliateId != null)
      criteria.add(Restrictions.eq("affiliate.id", affiliateId));
    if (state != null)
      criteria.add(Restrictions.eq("state", state));
    if (blocked != null)
      criteria.add(Restrictions.eq("blocked", blocked));
    if (moderation != null) {
      LogicalExpression or = Restrictions.or(Restrictions.isNull("blockReason"), Restrictions.eq("blockReason", "")); 
      if (moderation)
        criteria.add(or);
      else
        criteria.add(Restrictions.not(or));
    }
    
    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterable<OfferGrant> list(Ordering ord, boolean asc,
                                   int offset, int limit,
                                   OfferGrantFilter filter) {
    Criteria criteria = hiber().createCriteria(getEntityClass());

    fillCriteriaFromFilter(criteria, filter);

    setOrdering(criteria, ord, asc);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }

  @Override
  public long count(OfferGrantFilter filter) {
    Criteria criteria = hiber().createCriteria(getEntityClass());

    fillCriteriaFromFilter(criteria, filter);

    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  @Override
  public long count(Long offerId, Long affiliateId, OfferGrantState state, Boolean blocked, Boolean moderation) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (affiliateId != null)
      criteria.add(Restrictions.eq("affiliate.id", affiliateId));
    if (state != null)
      criteria.add(Restrictions.eq("state", state));
    if (blocked != null)
      criteria.add(Restrictions.eq("blocked", blocked));
    if (moderation != null) {
      LogicalExpression or = Restrictions.or(Restrictions.isNull("blockReason"), Restrictions.eq("blockReason", "")); 
      if (moderation)
        criteria.add(or);
      else
        criteria.add(Restrictions.not(or));
    }
    
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
    
    if (ord != Ordering.GRANT_ID)
      criteria.addOrder(order("id", asc));
  }

  private void fillCriteriaFromFilter(Criteria criteria, OfferGrantFilter filter) {
    addEqRestrictionIfNotNull(criteria, "offer.id", filter.offerId());
    addEqRestrictionIfNotNull(criteria, "affiliate.id", filter.affiliateId());
    addEqRestrictionIfNotNull(criteria, "state", filter.state());
    addEqRestrictionIfNotNull(criteria, "blocked", filter.blocked());

    if (filter.moderation() != null) {
      LogicalExpression or = Restrictions.or(
          Restrictions.isNull("blockReason"),
          Restrictions.eq("blockReason", ""));
      if (filter.moderation())
        criteria.add(or);
      else
        criteria.add(Restrictions.not(or));
    }

      if (filter.payMethod() != null) {
        criteria.createAlias("offer", "offer");
        Criterion parentPayMethodMatches =
            Restrictions.eq("offer.payMethod", filter.payMethod());
        Criterion subPayMethodMatches =
            Restrictions.sqlRestriction(
                "exists (select * from offer " +
                    "where parent_id = {alias}.offer_id and pay_method = ?)",
                filter.payMethod().toString(), StandardBasicTypes.STRING);
        criteria.add(Restrictions.or(parentPayMethodMatches, subPayMethodMatches));
      }

      for (String region : filter.regionList()) {
        criteria.add(Restrictions.sqlRestriction(
            "exists (select * from offer_region r " +
                "where {alias}.offer_id = r.offer_id and region = ?)",
            region, StandardBasicTypes.STRING));
      }

      for (Long category : filter.categoryIdList()) {
        criteria.add(Restrictions.sqlRestriction(
            "exists (select * from offer_category c " +
                "where {alias}.offer_id = c.offer_id and category_id = ?)",
            category, StandardBasicTypes.LONG));
      }
  }
  
}
