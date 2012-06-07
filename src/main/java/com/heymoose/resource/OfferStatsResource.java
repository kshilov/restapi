package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferStats;
import com.heymoose.domain.affiliate.OverallOfferStats;
import com.heymoose.domain.affiliate.Subs;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.OverallOfferStatsList;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.util.List;
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
  @Path("offers/all")
  @Transactional
  public OverallOfferStatsList offersAll(@QueryParam("source_id") String sourceId,
                                         @QueryParam("sub_id") String subId,
                                         @QueryParam("sub_id1") String subId1,
                                         @QueryParam("sub_id2") String subId2,
                                         @QueryParam("sub_id3") String subId3,
                                         @QueryParam("sub_id4") String subId4,
                                         @QueryParam("sub_group") String subGroup,
                                         @QueryParam("granted") @DefaultValue("false") boolean granted,
                                         @QueryParam("from") @DefaultValue("0") Long from,
                                         @QueryParam("to") Long to,
                                         @QueryParam("offset") @DefaultValue("0") int offset,
                                         @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    Subs subs = new Subs(sourceId, subId, subId1, subId2, subId3, subId4, subGroup);
    OverallOfferStatsList list = new OverallOfferStatsList();
    List<OverallOfferStats> overallOfferStats;
    if (granted)
      overallOfferStats = stats.grantedOfferStatsAll(subs, new DateTime(from), new DateTime(to), offset, limit);
    else
      overallOfferStats = stats.offerStatsAll(subs, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(overallOfferStats);
    list.count = list.stats.size();
    return list;
  }

  @GET
  @Path("offers/aff")
  @Transactional
  public OverallOfferStatsList offersAff(@QueryParam("aff_id") Long affId,
                                         @QueryParam("source_id") String sourceId,
                                         @QueryParam("sub_id") String subId,
                                         @QueryParam("sub_id1") String subId1,
                                         @QueryParam("sub_id2") String subId2,
                                         @QueryParam("sub_id3") String subId3,
                                         @QueryParam("sub_id4") String subId4,
                                         @QueryParam("sub_group") String subGroup,
                                         @QueryParam("from") @DefaultValue("0") Long from,
                                         @QueryParam("to") Long to,
                                         @QueryParam("offset") @DefaultValue("0") int offset,
                                         @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(affId);
    Subs subs = new Subs(sourceId, subId, subId1, subId2, subId3, subId4, subGroup);
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    List<OverallOfferStats> overallOfferStats = stats.offerStatsByAff(affId, subs, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(overallOfferStats);
    list.count = list.stats.size();
    return list;
  }

  @GET
  @Path("offers/adv")
  @Transactional
  public OverallOfferStatsList offersAdv(@QueryParam("adv_id") Long advId,
                                         @QueryParam("source_id") String sourceId,
                                         @QueryParam("sub_id") String subId,
                                         @QueryParam("sub_id1") String subId1,
                                         @QueryParam("sub_id2") String subId2,
                                         @QueryParam("sub_id3") String subId3,
                                         @QueryParam("sub_id4") String subId4,
                                         @QueryParam("sub_group") String subGroup,
                                         @QueryParam("from") @DefaultValue("0") Long from,
                                         @QueryParam("to") Long to,
                                         @QueryParam("offset") @DefaultValue("0") int offset,
                                         @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(advId);
    Subs subs = new Subs(sourceId, subId, subId1, subId2, subId3, subId4, subGroup);
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    List<OverallOfferStats> overallOfferStats = stats.offerStatsByAdv(advId, subs, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(overallOfferStats);
    list.count = list.stats.size();
    return list;
  }

  @GET
  @Path("affiliates/all")
  @Transactional
  public OverallOfferStatsList affAll(@QueryParam("source_id") String sourceId,
                                      @QueryParam("sub_id") String subId,
                                      @QueryParam("sub_id1") String subId1,
                                      @QueryParam("sub_id2") String subId2,
                                      @QueryParam("sub_id3") String subId3,
                                      @QueryParam("sub_id4") String subId4,
                                      @QueryParam("sub_group") String subGroup,
                                      @QueryParam("from") @DefaultValue("0") Long from,
                                      @QueryParam("to") Long to,
                                      @QueryParam("offset") @DefaultValue("0") int offset,
                                      @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    Subs subs = new Subs(sourceId, subId, subId1, subId2, subId3, subId4, subGroup);
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    List<OverallOfferStats> overallOfferStats = stats.affStats(subs, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(overallOfferStats);
    list.count = list.stats.size();
    return list;
  }

  @GET
  @Path("affiliates/offer")
  @Transactional
  public OverallOfferStatsList affOffers(@QueryParam("offer_id") Long offerId,
                                         @QueryParam("source_id") String sourceId,
                                         @QueryParam("sub_id") String subId,
                                         @QueryParam("sub_id1") String subId1,
                                         @QueryParam("sub_id2") String subId2,
                                         @QueryParam("sub_id3") String subId3,
                                         @QueryParam("sub_id4") String subId4,
                                         @QueryParam("sub_group") String subGroup,
                                         @QueryParam("from") @DefaultValue("0") Long from,
                                         @QueryParam("to") Long to,
                                         @QueryParam("offset") @DefaultValue("0") int offset,
                                         @QueryParam("limit") @DefaultValue("2147483647") int limit) {
    checkNotNull(offerId);
    Subs subs = new Subs(sourceId, subId, subId1, subId2, subId3, subId4, subGroup);
    if (to == null)
      to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    List<OverallOfferStats> overallOfferStats = stats.affStatsByOffer(offerId, subs, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(overallOfferStats);
    list.count = list.stats.size();
    return list;
  }
}
