package com.heymoose.resource;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.OfferStats;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.resource.xml.OverallOfferStatsList;
import com.heymoose.resource.xml.XmlQueryResult;
import com.heymoose.resource.xml.XmlTotalStats;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

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
  @Produces("application/xml")
  @Transactional
  public String allOfferStats(
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    Pair<QueryResult, Long> result = stats.allOfferStats(granted, common);
    return toOfferStatXml(result);
  }

  @GET
  @Path("offers/aff")
  @Produces("application/xml")
  @Transactional
  public String offersByAffiliateStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.affOfferStats(affId, common));
  }

  @GET
  @Path("offers/adv")
  @Produces("application/xml")
  @Transactional
  public String offersByAdvertiserStats(
      @QueryParam("adv_id") Long advId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(advId);
    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.advOfferStats(advId, common));
  }

  @GET
  @Path("affiliates/all")
  @Produces("application/xml")
  @Transactional
  public String allAffiliateStats(
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.affStats(common));
  }

  @GET
  @Path("advertisers/all")
  @Produces("application/xml")
  @Transactional
  public String allAdvertiserStats(
      @QueryParam("expired") @DefaultValue("false") boolean expired,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.advStats(common));
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
  @Produces("application/xml")
  @Transactional
  public String affiliateOfferStats(
      @QueryParam("offer_id") Long offerId,
      @QueryParam("for_advertiser") boolean forAdvertiser,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(offerId);
    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.affStatsByOffer(offerId, forAdvertiser, common));
  }

  @GET
  @Path("source_ids")
  @Produces("application/xml")
  @Transactional
  public String sourceIdStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.sourceIdStats(affId, offerId, common));
  }

  @GET
  @Path("sub_ids")
  @Produces("application/xml")
  @Transactional
  public String subIdStats(
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
    OfferStats.CommonParams common = new OfferStats.CommonParams();
    common.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subIdStats(
        affId, offerId, filter, groupBy, common));
  }


  @GET
  @Path("cashbacks")
  @Produces("application/xml")
  @Transactional
  public String cashbackStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);

    return toOfferStatXml(stats.cashbackStats(affId, filter));
  }

  @GET
  @Path("suboffers")
  @Produces("application/xml")
  @Transactional
  public String subofferStatByOffer(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("for_advertiser") boolean forAdvertiser,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(offerId);
    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForOffer(
        affId, offerId, forAdvertiser, filter));
  }

  @GET
  @Path("suboffers/affiliate")
  @Produces("application/xml")
  @Transactional
  public String subofferStatByAffiliate(
      @QueryParam("aff_id") Long affId,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForAffiliate(affId, filter));
  }

  @GET
  @Path("suboffers/advertiser")
  @Produces("application/xml")
  @Transactional
  public String subofferStatByAdvertiser(
      @QueryParam("adv_id") Long advId,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(advId);
    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForAdvertiser(advId, filter));
  }

  @GET
  @Path("suboffers/sub_id")
  @Produces("application/xml")
  @Transactional
  public String subofferStatBySubIds(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("sub_id") String subId,
      @QueryParam("sub_id1") String subId1,
      @QueryParam("sub_id2") String subId2,
      @QueryParam("sub_id3") String subId3,
      @QueryParam("sub_id4") String subId4,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    ImmutableMap.Builder<String, String> filterBuilder = ImmutableMap.builder();
    putIfNotNull(filterBuilder, "sub_id", subId);
    putIfNotNull(filterBuilder, "sub_id1", subId1);
    putIfNotNull(filterBuilder, "sub_id2", subId2);
    putIfNotNull(filterBuilder, "sub_id3", subId3);
    putIfNotNull(filterBuilder, "sub_id4", subId4);

    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForSubIds(
        affId, offerId, filterBuilder.build(), filter));
  }

  @GET
  @Path("suboffers/source_id")
  @Produces("application/xml")
  @Transactional
  public String subofferStatBySourceId(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("source_id") String sourceId,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForSourceId(
        affId, offerId, sourceId, filter));
  }

  @GET
  @Path("suboffers/referer")
  @Produces("application/xml")
  @Transactional
  public String subofferStatByReferer(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("referer") String referer,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForReferer(
        affId, offerId, referer, filter));
  }

  @GET
  @Path("suboffers/keywords")
  @Produces("application/xml")
  @Transactional
  public String subofferStatByKeywords(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("keywords") String keywords,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForKeywords(
        affId, offerId, keywords, filter));
  }

  @GET
  @Path("suboffers/cashback")
  @Produces("application/xml")
  @Transactional
  public String subofferStatByCashback(
      @QueryParam("aff_id") Long affId,
      @QueryParam("cashback") String cashback,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    checkNotNull(affId);
    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.subofferStatForCashback(
        affId, Strings.nullToEmpty(cashback), filter));

  }

  @GET
  @Path("referer")
  @Produces("application/xml")
  @Transactional
  public String refererStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.refererStats(
        affId, offerId, filter));
  }

  @GET
  @Path("keywords")
  @Produces("application/xml")
  @Transactional
  public String keywordsStats(
      @QueryParam("aff_id") Long affId,
      @QueryParam("offer_id") Long offerId,
      @QueryParam("granted") @DefaultValue("false") boolean granted,
      @QueryParam("from") @DefaultValue("0") Long from,
      @QueryParam("to") Long to,
      @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction) {

    OfferStats.CommonParams filter = new OfferStats.CommonParams();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toOfferStatXml(stats.keywordsStats(
        affId, offerId, filter));
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

  private String toOfferStatXml(Pair<QueryResult, Long> result) {
    return new XmlQueryResult(result.fst)
        .setRoot("stats")
        .setElement("stat")
        .addRootAttribute("count", result.snd)
        .addFieldMapping("descr", "name")
        .addFieldMapping("clicks_count", "clicks")
        .addFieldMapping("shows_count", "shows")
        .addFieldMapping("leads_count", "leads")
        .addFieldMapping("sales_count", "sales")
        .addFieldMapping("confirmed_revenue", "confirmed-revenue")
        .addFieldMapping("not_confirmed_revenue", "not-confirmed-revenue")
        .addFieldMapping("canceled_revenue", "canceled-revenue")
        .toString();
  }

}
