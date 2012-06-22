package com.heymoose.resource;

import com.heymoose.domain.affiliate.OfferStats;
import com.heymoose.domain.affiliate.OverallOfferStats;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.util.Pair;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import static java.util.Arrays.asList;
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
  @Path("advertisers/all")
  @Transactional
  public OverallOfferStatsList allAdvertiserStats(
      @QueryParam("expired") @DefaultValue("false") boolean expired,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p = stats.advStats(expired, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("affiliates/top")
  @Transactional
  public OverallOfferStatsList topAffiliateStats(
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    list.stats = stats.topAffiliates(new DateTime(from), new DateTime(to), offset, limit);
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
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.sourceIdStats(granted, affId, offerId, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("sub_ids")
  @Transactional
  public OverallOfferStatsList subIdStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("sub_id") String subId,
      @QueryParam("sub_id1") String subId1,
      @QueryParam("sub_id2") String subId2,
      @QueryParam("sub_id3") String subId3,
      @QueryParam("sub_id4") String subId4,
      @QueryParam("g_sub_id") @DefaultValue("false") boolean groupSubId,
      @QueryParam("g_sub_id1") @DefaultValue("false") boolean groupSubId1,
      @QueryParam("g_sub_id2") @DefaultValue("false") boolean groupSubId2,
      @QueryParam("g_sub_id3") @DefaultValue("false") boolean groupSubId3,
      @QueryParam("g_sub_id4") @DefaultValue("false") boolean groupSubId4,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p = stats.subIdStats(
        granted, affId, offerId,
        asList(subId, subId1, subId2, subId3, subId4),
        asList(groupSubId, groupSubId1, groupSubId2, groupSubId3, groupSubId4),
        new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("referer")
  @Transactional
  public OverallOfferStatsList refererStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.refererStats(granted, affId, offerId, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("keywords")
  @Transactional
  public OverallOfferStatsList keywordsStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    Pair<List<OverallOfferStats>, Long> p
        = stats.keywordsStats(granted, affId, offerId, new DateTime(from), new DateTime(to), offset, limit);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }
}
