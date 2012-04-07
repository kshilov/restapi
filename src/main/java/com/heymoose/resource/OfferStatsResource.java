package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferStats;
import com.heymoose.domain.affiliate.OverallOfferStats;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.OverallOfferStatsList;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Singleton
@Path("stats")
public class OfferStatsResource {

  private final OfferStats stats;

  @Inject
  public OfferStatsResource(OfferStats stats) {
    this.stats = stats;
  }

  @GET
  @Transactional
  public OverallOfferStatsList get(@QueryParam("aff_id") Long affId,
                                   @QueryParam("ordering") @DefaultValue("OFFER") OfferStats.Ordering ordering,
                                   @QueryParam("dir") @DefaultValue("ASC") OfferStats.Dir dir,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(affId);
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.affStats(affId, ordering, dir, offset, limit))
      list.stats.add(s);
    list.count = stats.countAffStats(affId);
    return list;
  }
}
