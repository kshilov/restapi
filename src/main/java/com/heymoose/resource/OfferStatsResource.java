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
  @Path("all")
  @Transactional
  public OverallOfferStatsList all(@QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.statsAll(offset, limit))
      list.stats.add(s);
    list.count = stats.countAll();
    return list;
  }

  @GET
  @Path("aff")
  @Transactional
  public OverallOfferStatsList aff(@QueryParam("aff_id") Long affId,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(affId);
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.statsAff(affId, offset, limit))
      list.stats.add(s);
    list.count = stats.countAff(affId);
    return list;
  }

  @GET
  @Path("adv")
  @Transactional
  public OverallOfferStatsList adv(@QueryParam("adv_id") Long advId,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(advId);
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.statsAdv(advId, offset, limit))
      list.stats.add(s);
    list.count = stats.countAdv(advId);
    return list;
  }
}
