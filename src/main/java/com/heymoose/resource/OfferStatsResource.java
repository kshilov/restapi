package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferStats;
import com.heymoose.domain.affiliate.OverallOfferStats;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.util.Pair;
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
  public OverallOfferStatsList allOfferStats(
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.offerStats(granted, null, null, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("offers/aff")
  @Transactional
  public OverallOfferStatsList offersByAffiliateStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    checkNotNull(affId);
    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.offerStats(granted, affId, null, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("offers/adv")
  @Transactional
  public OverallOfferStatsList offersByAdvertizerStats(
      @QueryParam("adv_id") Long advId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    checkNotNull(advId);
    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.offerStats(granted, null, advId, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("affiliates/all")
  @Transactional
  public OverallOfferStatsList allAffiliateStats(
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p = stats.affStats(granted, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("affiliates/offer")
  @Transactional
  public OverallOfferStatsList affiliateOfferStats(
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    checkNotNull(offerId);
    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.affStatsByOffer(granted, offerId, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("source_ids")
  @Transactional
  public OverallOfferStatsList sourceIdStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.sourceIdStats(granted, affId, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }
}
