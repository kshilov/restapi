package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.OfferSite;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.site.SiteAttribute;
import com.heymoose.infrastructure.util.Cacheable;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class Sites {

  public enum Ordering {
    ID, AFFILIATE_EMAIL, NAME, TYPE, APPROVED
  }

  public enum StatOrdering {
    AFFILIATE_EMAIL, REFERER,
    FIRST_PERIOD_CLICK_COUNT, FIRST_PERIOD_SHOW_COUNT,
    SECOND_PERIOD_CLICK_COUNT, SECOND_PERIOD_SHOW_COUNT,
    CLICK_COUNT_DIFF, SHOW_COUNT_DIFF
  }

  private final Repo repo;

  @Inject
  public Sites(Repo repo) {
    this.repo = repo;
  }

  @Cacheable(period = "PT1H")
  public QueryResult stats(
      DateTime firstFromDate, DateTime firstToDate,
      DateTime secondFromDate, DateTime secondToDate,
      boolean removedOnly,
      StatOrdering ordering, OrderingDirection direction,
      int offset, int limit) {
    return SqlLoader.templateQuery("site-stats", repo.session())
        .addQueryParam("first_period_from", firstFromDate.toDate())
        .addQueryParam("first_period_to", firstToDate.toDate())
        .addQueryParam("second_period_from", secondFromDate.toDate())
        .addQueryParam("second_period_to", secondToDate.toDate())
        .addTemplateParam("removedOnly", removedOnly)
        .addTemplateParam("ordering", ordering.toString())
        .addTemplateParam("direction", direction.toString())
        .execute(offset, limit);
  }

  @Cacheable(period = "PT1H")
  public Long statsCount(
      DateTime firstFromDate, DateTime firstToDate,
      DateTime secondFromDate, DateTime secondToDate,
      boolean removedOnly) {
    return SqlLoader.templateQuery("site-stats", repo.session())
        .addQueryParam("first_period_from", firstFromDate.toDate())
        .addQueryParam("first_period_to", firstToDate.toDate())
        .addQueryParam("second_period_from", secondFromDate.toDate())
        .addQueryParam("second_period_to", secondToDate.toDate())
        .addTemplateParam("removedOnly", removedOnly)
        .count();
  }

  public Site add(Site site) {
    repo.put(site);
    for (SiteAttribute attr : site.attributeList()) repo.put(attr);
    return site;
  }

  public Site put(Site site) {
    repo.put(site);
    return site;
  }

  public OfferSite placeOffer(Offer offer, Site site) {
    OfferSite offerSite = new OfferSite()
        .setOffer(offer)
        .setSite(site);
    repo.put(offerSite);
    return offerSite;
  }

  public Site approvedSite(Long siteId) {
    return repo.byHQL(Site.class,
        "from Site where id = ? and approvedByAdmin = true", siteId);
  }

  public Site get(Long siteId) {
    return repo.get(Site.class, siteId);
  }

  public void put(OfferSite offerSite) {
    repo.put(offerSite);
  }

  public OfferSite getOfferSite(Long id) {
    return repo.get(OfferSite.class, id);
  }

  @SuppressWarnings("unchecked")
  public Pair<List<Site>, Long> list(Long affId, DataFilter<Ordering> common) {
    if (common.limit() == 0) return Pair.of(Collections.<Site>emptyList(), 0L);
    Criteria c = repo.session().createCriteria(Site.class);
    if (affId != null) c.add(Restrictions.eq("affId", affId));
    c.setFirstResult(common.offset());
    c.setMaxResults(common.limit());
    switch (common.ordering()) {
      case AFFILIATE_EMAIL:
        c.createAlias("affiliate", "affiliate");
        addOrder(c, "affiliate.email", common.direction());
        break;
      case NAME:
        addOrder(c, "name", common.direction());
        break;
      case ID:
        addOrder(c, "id", common.direction());
        break;
      case TYPE:
        addOrder(c, "type", common.direction());
        break;
      case APPROVED:
        addOrder(c, "approvedByAdmin", common.direction());
        break;
    }
    List<Site> result = (List<Site>) c.list();

    Criteria countQuery = repo.session().createCriteria(Site.class);
    if (affId != null) countQuery.add(Restrictions.eq("affId", affId));
    countQuery.setProjection(Projections.count("id"));
    Long count = (Long) countQuery.uniqueResult();
    return Pair.of(result, count);
  }

  private void addOrder(Criteria c, String name, OrderingDirection direction) {
    switch (direction) {
      case ASC :
        c.addOrder(Order.asc(name));
        break;
      case DESC:
        c.addOrder(Order.desc(name));
        break;
    }
  }

  public OfferSite checkPermission(BaseOffer offer, Site site) {
    if (!site.approvedByAdmin())
      throw new IllegalArgumentException(
          "Site " + site + " not approved by admin");
    OfferSite offerSite = repo.byHQL(OfferSite.class,
        "from OfferSite where offer = ? and site = ? and approved = true",
        offer.masterOffer(), site);
    if (offerSite == null) {
      throw new IllegalStateException("No permission for offer " +
          offer + " and site " + site);
    }
    return offerSite;
  }


  public Pair<QueryResult, Long> listOfferSites(Long affId, Long offerId,
                                                int offset, int limit) {
    return SqlLoader.templateQuery("offer-site-list", repo.session())
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addQueryParamIfNotNull(offerId, "offer_id", offerId)
        .executeAndCount(offset, limit);
  }


}
