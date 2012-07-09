package com.heymoose.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.heymoose.domain.affiliate.OfferStats;
import com.heymoose.domain.affiliate.OverallOfferStats;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.resource.xml.XmlTotalStats;
import com.heymoose.util.OrderingDirection;
import com.heymoose.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.heymoose.util.WebAppUtil.checkNotNull;

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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.allOfferStats(granted, common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.affOfferStats(affId, common);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("offers/adv")
  @Transactional
  public OverallOfferStatsList offersByAdvertiserStats(
      @QueryParam("adv_id") Long advId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(advId);
    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.advOfferStats(advId, common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.affStats(common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.advStats(common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(offerId);
    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.affStatsByOffer(offerId, common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.sourceIdStats(
        affId, offerId, common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    ImmutableMap.Builder<String, String> filterBuilder = ImmutableMap.builder();
    putIfNotNull(filterBuilder, "sub_id", subId);
    putIfNotNull(filterBuilder, "sub_id1", subId1);
    putIfNotNull(filterBuilder, "sub_id2", subId2);
    putIfNotNull(filterBuilder, "sub_id3", subId3);
    putIfNotNull(filterBuilder, "sub_id4", subId4);
    ImmutableSet.Builder<String> groupBySetBuilder = ImmutableSet.builder();
    addIfTrue(groupBySetBuilder, "sub_id", groupSubId);
    addIfTrue(groupBySetBuilder, "sub_id1", groupSubId1);
    addIfTrue(groupBySetBuilder, "sub_id2", groupSubId2);
    addIfTrue(groupBySetBuilder, "sub_id3", groupSubId3);
    addIfTrue(groupBySetBuilder, "sub_id4", groupSubId4);

    Map<String, String> filter = filterBuilder.build();
    // not group by filter fields
    Set<String> groupBy = Sets.difference(
        groupBySetBuilder.build(),
        filter.entrySet());
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.subIdStats(
        affId, offerId, filter, groupBy, common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p =stats.refererStats(
        affId, offerId, common);
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    if (to == null) to = DateTimeUtils.currentTimeMillis();
    OverallOfferStatsList list = new OverallOfferStatsList();
    OfferStats.CommonParams common = new OfferStats.CommonParams(
        new DateTime(from), new DateTime(to),
        offset, limit, ordering, direction);
    Pair<List<OverallOfferStats>, Long> p = stats.keywordsStats(
        affId, offerId, common);
    list.stats.addAll(p.fst);
    list.count = p.snd;
    return list;
  }

  @GET
  @Path("total")
  @Transactional
  public XmlTotalStats totalStats(
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to) {
    Map<String, BigDecimal> statsData = stats.totalStats(
        new DateTime(from), new DateTime(to));
    return new XmlTotalStats(statsData);
  }

  private void putIfNotNull(ImmutableMap.Builder<String, String> builder,
                            String key, String value) {
    if (value != null) {
      builder.put(key, value);
    }
  }

  private void addIfTrue(ImmutableSet.Builder<String> builder,
                         String value, boolean test) {
    if (test) {
      builder.add(value);
    }
  }

}
