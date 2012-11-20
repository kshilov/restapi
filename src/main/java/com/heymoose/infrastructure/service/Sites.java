package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.OfferSite;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.site.SiteAttribute;
import com.heymoose.infrastructure.util.Cacheable;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
import org.joda.time.DateTime;

public class Sites {

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


}
