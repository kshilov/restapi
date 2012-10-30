package com.heymoose.infrastructure.persistence;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantFilter;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.grant.OfferGrantState;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.OfferRepository.Ordering;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.util.OrderingDirection;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.heymoose.infrastructure.util.HibernateUtil.*;

public class OfferGrantRepositoryHiber extends RepositoryHiber<OfferGrant> implements
    OfferGrantRepository {

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
  @SuppressWarnings("unchecked")
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
  @SuppressWarnings("unchecked")
  public Iterable<OfferGrant> list(Ordering ord, OrderingDirection direction,
                                   int offset, int limit,
                                   OfferGrantFilter filter) {
    Criteria criteria = hiber().createCriteria(getEntityClass());

    fillCriteriaFromFilter(criteria, filter);

    setOrdering(criteria, ord, direction);
    return criteria
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }



  @Override
  public Iterable<Offer> exclusiveGrantedOffers(Long affId) {
    Preconditions.checkNotNull(affId, "Affiliate can not be null.");
    OfferGrantFilter filter = new OfferGrantFilter()
        .setAffiliateId(affId)
        .setBlocked(false)
        .setActive(true)
        .setProductOffersOnly(true)
        .setState(OfferGrantState.APPROVED);
    ImmutableSet.Builder<Offer> result = ImmutableSet.builder();
    for (OfferGrant grant: this.list(
        OfferRepository.Ordering.ID,
        OrderingDirection.ASC,
        0, Integer.MAX_VALUE, filter)) {
      result.add(grant.offer());
    }
    return result.build();
  }



  @Override
  public long count(OfferGrantFilter filter) {
    Criteria criteria = hiber().createCriteria(getEntityClass());

    fillCriteriaFromFilter(criteria, filter);

    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }


  private static void setOrdering(Criteria criteria, Ordering ord,
                                  OrderingDirection direction) {
    switch (ord) {
    case GRANT_ID: criteria.addOrder(order("id", direction)); break;
    case GRANT_APPROVED: criteria.addOrder(order("approved", direction)); break;
    case GRANT_ACTIVE: criteria.addOrder(order("active", direction)); break;
    case NAME: criteria
      .createAlias("offer", "offer")
      .addOrder(order("offer.name", direction));
    break;
    case GRANT_AFFILIATE_LAST_NAME: criteria
      .createAlias("affiliate", "affiliate")
      .addOrder(order("affiliate.lastName", direction));
    break;
    case AFFILIATE_ID: criteria
          .createAlias("affiliate", "affiliate")
          .addOrder(order("affiliate.id", direction));
      break;
    }
    
    if (ord != Ordering.GRANT_ID)
      criteria.addOrder(order("id", direction));
  }

  private void fillCriteriaFromFilter(Criteria criteria, OfferGrantFilter filter) {
    criteria.createAlias("offer", "offer");
    addEqRestrictionIfNotNull(criteria, "offer.id", filter.offerId());
    addEqRestrictionIfNotNull(criteria, "affiliate.id", filter.affiliateId());
    addEqRestrictionIfNotNull(criteria, "state", filter.state());
    addEqRestrictionIfNotNull(criteria, "blocked", filter.blocked());
    if (filter.exclusiveOnly())
      criteria.add(Restrictions.eq("offer.exclusive", true));
    if (filter.productOffersOnly())
      criteria.add(Restrictions.eq("offer.isProductOffer", true));

    if (filter.moderation() != null) {
      LogicalExpression or = Restrictions.or(
          Restrictions.isNull("blockReason"),
          Restrictions.eq("blockReason", ""));
      if (filter.moderation())
        criteria.add(or);
      else
        criteria.add(Restrictions.not(or));
    }

      if (filter.payMethod() == PayMethod.CPA) {
        criteria.createAlias("offer", "offer");
        Criterion parentPayMethodMatches = Restrictions.and(
            Restrictions.eq("offer.payMethod", filter.payMethod()),
            Restrictions.eq("offer.cpaPolicy", filter.cpaPolicy()));
        Criterion subPayMethodMatches =
            Restrictions.sqlRestriction(
                "exists (select * from offer " +
                    "where parent_id = {alias}.offer_id " +
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
      criteria.createAlias("offer", "offer");
      Criterion parentPayMethodMatches =
          Restrictions.eq("offer.payMethod", filter.payMethod());
      Criterion subPayMethodMatches =
          Restrictions.sqlRestriction(
              "exists (select * from offer " +
                  "where parent_id = {alias}.offer_id " +
                  "and pay_method = ? )",
              filter.payMethod().toString(),
              StandardBasicTypes.STRING);
      criteria.add(Restrictions.or(parentPayMethodMatches, subPayMethodMatches));
    }

    String existsRegion = "exists (select * from offer_region r " +
        "where {alias}.offer_id = r.offer_id and region in (?))";
    addSqlInRestriction(criteria, existsRegion, filter.regionList(),
        StandardBasicTypes.STRING);

    String existsCategory = "exists (select * from offer_category c " +
        "where {alias}.offer_id = c.offer_id and category_id in (?))";
    addSqlInRestriction(criteria, existsCategory, filter.categoryIdList(),
        StandardBasicTypes.LONG);
  }


  @Override
  public OfferGrant checkGrant(User user, BaseOffer offer) {
    OfferGrant grant = this.visibleByOfferAndAff(offer, user);
    if (grant == null) {
      throw new IllegalStateException("Offer not granted: " + offer.id());
    }
    return grant;
  }


}
