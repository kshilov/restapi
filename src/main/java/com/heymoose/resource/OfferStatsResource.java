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
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

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
  public OverallOfferStatsList all(@QueryParam("from") @DefaultValue("0") Long from,
                                   @QueryParam("to") Long to,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.statsAll(new DateTime(from), new DateTime(to), offset, limit))
      list.stats.add(s);
    list.count = stats.countAll(new DateTime(from), new DateTime(to));
    return list;
  }

  @GET
  @Path("aff")
  @Transactional
  public OverallOfferStatsList aff(@QueryParam("aff_id") Long affId,
                                   @QueryParam("from") @DefaultValue("0") Long from,
                                   @QueryParam("to") Long to,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(affId);
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.statsAff(affId, new DateTime(from), new DateTime(to), offset, limit))
      list.stats.add(s);
    list.count = stats.countAff(affId, new DateTime(from), new DateTime(to));
    return list;
  }

  @GET
  @Path("adv")
  @Transactional
  public OverallOfferStatsList adv(@QueryParam("adv_id") Long advId,
                                   @QueryParam("from") @DefaultValue("0") Long from,
                                   @QueryParam("to") Long to,
                                   @QueryParam("offset") @DefaultValue("0") int offset,
                                   @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(advId);
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    for (OverallOfferStats s : stats.statsAdv(advId, new DateTime(from), new DateTime(to), offset, limit))
      list.stats.add(s);
    list.count = stats.countAdv(advId, new DateTime(from), new DateTime(to));
    return list;
  }
}
