package com.heymoose.infrastructure.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.Placement;
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
import java.util.Map;
import java.util.Set;

public class Sites {

  public enum Ordering {
    ID, AFFILIATE_EMAIL, NAME, TYPE, ADMIN_STATE, DESCRIPTION, CREATION_TIME,
    LAST_CHANGE_TIME, APPROVED
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
    for (SiteAttribute attr : site.attributeList()) {
      repo.put(attr);
    }
    return site;
  }

  public Placement addPlacement(Placement placement) {
    repo.put(placement);
    return placement;
  }

  public Site approvedSite(Long siteId) {
    return repo.byHQL(Site.class,
        "from Site where id = ? and adminState = 'APPROVED'", siteId);
  }

  public Site get(Long siteId) {
    return repo.get(Site.class, siteId);
  }

  public void put(Placement placement) {
    repo.put(placement);
  }

  public Placement getOfferSite(Long id) {
    return repo.get(Placement.class, id);
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
      case ADMIN_STATE:
        addOrder(c, "adminState", common.direction());
        break;
      case DESCRIPTION:
        addOrder(c, "description", common.direction());
        break;
      case CREATION_TIME:
        addOrder(c, "creationTime", common.direction());
        break;
      case LAST_CHANGE_TIME:
        addOrder(c, "lastChangeTime", common.direction());
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

  public Placement checkPermission(BaseOffer offer, Site site) {
    if (!site.approvedByAdmin())
      throw new IllegalArgumentException(
          "Site " + site + " not approved by admin");
    Placement placement = repo.byHQL(Placement.class,
        "from Placement where offer = ? and site = ? and adminState = 'APPROVED'",
        offer.masterOffer(), site);
    if (placement == null) {
      throw new IllegalStateException("No permission for offer " +
          offer + " and site " + site);
    }
    return placement;
  }


  public Pair<QueryResult, Long> listPlacements(Long affId, Long offerId,
                                                int offset, int limit) {
    return SqlLoader.templateQuery("placement-list", repo.session())
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addQueryParamIfNotNull(offerId, "offer_id", offerId)
        .executeAndCount(offset, limit);
  }

  public void merge(Site from, Site to) {
    to.setName(from.name())
        .setType(from.type())
        .setDescription(from.description())
        .setAdminState(AdminState.MODERATION)
        .touch();
    List<SiteAttribute> toAttrList = to.attributeList();
    Map<String, String> fromAttrMap = from.attributeMap();
    for (SiteAttribute attr : toAttrList) {
      if (!fromAttrMap.containsKey(attr.key())) {
        repo.remove(attr);
        continue;
      }
      if (!fromAttrMap.get(attr.key()).equals(attr.value())) {
        attr.setValue(fromAttrMap.get(attr.key()));
        repo.put(attr);
      }
    }
    Set<String> unTrackedKeys = Sets.difference(
        fromAttrMap.keySet(),
        to.attributeMap().keySet());
    for (String key : unTrackedKeys) {
      repo.put(new SiteAttribute()
          .setSite(to)
          .setKey(key)
          .setValue(fromAttrMap.get(key)));
    }
    repo.put(to);
  }

  public void moderate(Placement placement,
                       AdminState state,
                       String adminComment) {
    placement.setAdminState(state).setAdminComment(adminComment).touch();
    repo.put(placement);
  }

  public void moderate(Site site,
                       AdminState state,
                       String adminComment) {
    site.setAdminState(state).setAdminComment(adminComment).touch();
    repo.put(site);
  }

  public List<Placement> placementList(long offerId, long affiliateId) {
    return repo.allByHQL(Placement.class,
        "from Placement where offer.id = ? and site.affId = ?",
        offerId, affiliateId);
  }

  public Long placementCount(long offerId, long affiliateId) {
    return SqlLoader.templateQuery("placement-list", repo.session())
        .addTemplateParam("filterByAffiliate", true)
        .addQueryParam("aff_id", affiliateId)
        .addTemplateParam("filterByOffer", true)
        .addQueryParam("offer_id", offerId)
        .executeAndCount(0, 1).snd;
  }

  public Placement findPlacement(Site site, Offer offer) {
    return repo.byHQL(Placement.class,
        "from Placement where site = ? and offer = ?", site, offer);
  }

  public Map<String, Object> getPlacementAsMap(Long id) {
    Pair<QueryResult, Long> result = SqlLoader
        .templateQuery("placement-list", repo.session())
        .addTemplateParam("filterById", true)
        .addQueryParam("id", id)
        .executeAndCount(0, 1);
    if (result.snd.equals(0L)) return ImmutableMap.of();
    return result.fst.get(0);
  }

  public void removeOfferSite(Placement placement) {
    repo.session().delete(placement);
  }


}
