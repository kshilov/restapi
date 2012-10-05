package com.heymoose.resource;

import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.AffiliateStats;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.resource.xml.XmlFraudStat;
import com.heymoose.resource.xml.XmlQueryResult;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.math.BigDecimal;

@Path("user-stats")
public class UserStatsResource {

  private final AffiliateStats affiliateStats;

  @Inject
  public UserStatsResource(AffiliateStats affiliateStats) {
    this.affiliateStats = affiliateStats;
  }

  @GET
  @Path("fraud")
  @Transactional
  public XmlFraudStat fraudStat(
      @QueryParam("active") @DefaultValue("true") boolean activeOnly,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("RATE")
      AffiliateStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC")
      OrderingDirection direction) {
    DataFilter<AffiliateStats.Ordering> filter =
        new DataFilter<AffiliateStats.Ordering>()
        .setFrom(from)
        .setTo(to)
        .setOrdering(ordering)
        .setDirection(direction)
        .setOffset(offset)
        .setLimit(limit);
    Pair<QueryResult, Long> pair =
        affiliateStats.fraudStat(activeOnly, offerId, filter);
    return new XmlFraudStat(pair);
  }

  @GET
  @Path("{id}/referrals")
  @Produces("application/xml")
  @Transactional
  public String referralStat(@PathParam("id") Long affId,
                             @QueryParam("source") String source,
                             @QueryParam("offset") int offset,
                             @QueryParam("limit") @DefaultValue("20") int limit,
                             @QueryParam("ordering") @DefaultValue("AMOUNT")
                             AffiliateStats.ReferralOrdering ordering,
                             @QueryParam("direction") @DefaultValue("DESC")
                             OrderingDirection direction) {

    DataFilter<AffiliateStats.ReferralOrdering> filter =
        new DataFilter<AffiliateStats.ReferralOrdering>()
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    Pair<QueryResult, Long> result =
        affiliateStats.referralStat(affId, source, filter);
    BigDecimal sum = affiliateStats.sumReferralStat(affId, source);
    return new XmlQueryResult(result.fst)
        .setRoot("stats")
        .addRootAttribute("count", result.snd)
        .addRootAttribute("sum", sum)
        .setElement("stat")
        .toString();
  }

}
