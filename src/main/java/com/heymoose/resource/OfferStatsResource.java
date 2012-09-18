package com.heymoose.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.OfferStats;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Singleton
@Path("stats")
public class OfferStatsResource {

  private static final String STATS = "stats";
  private static final String STAT = "stat";

  private static String toXml(Pair<QueryResult, Long> p) {
    return new XmlQueryResult(p)
        .setRoot(STATS)
        .setElement(STAT)
        .toString();
  }

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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    return toXml(stats.allOfferStats(granted, parseContext(context)));

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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(affId);
    return toXml(stats.affOfferStats(affId, parseContext(context)));
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("DESCR") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(advId);
    return toXml(stats.advOfferStats(advId, parseContext(context)));
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    return toXml(stats.affStats(parseContext(context)));
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
      @QueryParam("limit") @DefaultValue("2147483647") int limit,
      @QueryParam("ordering") @DefaultValue("CLICKS_COUNT") OfferStats.Ordering ordering,
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    return toXml(stats.advStats(parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(offerId);
    return toXml(stats.affStatsByOffer(
        offerId, forAdvertiser, parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    return toXml(stats.sourceIdStats(affId, offerId, parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

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
    return toXml(stats.subIdStats(
        affId, offerId, filter, groupBy, parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(offerId);
    return toXml(stats.subofferStatForOffer(
        affId, offerId, forAdvertiser, parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(affId);
    return toXml(
        stats.subofferStatForAffiliate(affId, parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(advId);
    Pair<QueryResult, Long> p = stats.subofferStatForAdvertiser(
        advId,  parseContext(context));
    return toXml(p);
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(affId);
    ImmutableMap.Builder<String, String> filterBuilder = ImmutableMap.builder();
    putIfNotNull(filterBuilder, "sub_id", subId);
    putIfNotNull(filterBuilder, "sub_id1", subId1);
    putIfNotNull(filterBuilder, "sub_id2", subId2);
    putIfNotNull(filterBuilder, "sub_id3", subId3);
    putIfNotNull(filterBuilder, "sub_id4", subId4);

    Pair<QueryResult, Long> p = stats.subofferStatForSubIds(
        affId, offerId, filterBuilder.build(), parseContext(context));
    return toXml(p);
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    checkNotNull(affId);
    Pair<QueryResult, Long> p = stats.subofferStatForSourceId(
        affId, offerId, sourceId, parseContext(context));
    return toXml(p);
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    Pair<QueryResult, Long> p = stats.subofferStatForReferer(
        affId, offerId, referer, parseContext(context));
    return toXml(p);
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    Pair<QueryResult, Long> p = stats.subofferStatForKeywords(
        affId, offerId, keywords, parseContext(context));
    return toXml(p);
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    return toXml(stats.refererStats(
        affId, offerId, parseContext(context)));
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
      @QueryParam("direction") @DefaultValue("DESC") OrderingDirection direction,
      @Context UriInfo context) {

    return toXml(stats.keywordsStats(
        affId, offerId, parseContext(context)));
  }

  @GET
  @Path("total")
  @Produces("application/xml")
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

  private DataFilter<OfferStats.Ordering> parseContext(UriInfo context) {
    MultivaluedMap<String, String> queryParams = context.getQueryParameters();
    DataFilter<OfferStats.Ordering> filter = DataFilter.newInstance();
    Long from = new Long(nullToDefault(queryParams, "from", "0"));
    String toString = queryParams.getFirst("to");
    Long to = null;
    if (toString != null) {
      to = Long.parseLong(queryParams.getFirst("to"));
    }
    return filter.setFrom(new DateTime())
        .setTo(to)
        .setFrom(from)
        .setOffset(new Integer(nullToDefault(queryParams, "offset", "0")))
        .setLimit(new Integer(nullToDefault(queryParams, "limit", "20")))
        .setOrdering(OfferStats.Ordering.valueOf(
            nullToDefault(queryParams, "ordering", "DESCR")))
        .setDirection(OrderingDirection.valueOf(
            nullToDefault(queryParams, "direction", "DESC")));
  }

  private String nullToDefault(MultivaluedMap<String, String> map,
                               String key,
                               String defaultValue) {
    if (map.getFirst(key) == null) return defaultValue;
    return map.getFirst(key);
  }


}
