package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.ImmutableMapTransformer;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.infrastructure.util.SqlLoader;
import com.heymoose.resource.xml.XmlOverallOfferStats;
import org.hibernate.Query;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class OfferStats {

  public static enum Ordering {
    DESCR, SHOWS_COUNT, CLICKS_COUNT, LEADS_COUNT, SALES_COUNT,
    CONFIRMED_REVENUE, NOT_CONFIRMED_REVENUE, CANCELED_REVENUE,
    CTR, CR, ECPC, ECPM
  }

  public static final class CommonParams {
    private final DateTime from;
    private final DateTime to;
    private final int offset;
    private final int limit;
    private final Ordering ordering;
    private final OrderingDirection direction;

    public CommonParams(DateTime from, DateTime to,
                        int offset, int limit,
                        Ordering ordering, OrderingDirection direction) {
      this.from = from;
      this.to = to;
      this.offset = offset;
      this.limit = limit;
      this.ordering = ordering;
      this.direction = direction;
    }

  }

  public static final String CANCELED_SUM = "canceled_sum";
  public static final String CANCELED_AFFILIATE = "canceled_affiliate";
  public static final String CANCELED_FEE = "canceled_fee";
  public static final String NOT_CONFIRMED_AFFILIATE = "not_confirmed_affiliate";
  public static final String NOT_CONFIRMED_FEE = "not_confirmed_fee";
  public static final String NOT_CONFIRMED_SUM = "not_confirmed_sum";
  public static final String CONFIRMED_AFFILIATE = "confirmed_affiliate";
  public static final String CONFIRMED_FEE = "confirmed_fee";
  public static final String CONFIRMED_SUM = "confirmed_sum";
  public static final String EXPIRED_AFFILIATE = "expired_affiliate";
  public static final String EXPIRED_FEE = "expired_fee";
  public static final String EXPIRED_SUM = "expired_sum";

  //private final Provider<Session> sessionProvider;
  private final Repo repo;

  @Inject
  public OfferStats(/*Provider<Session> sessionProvider,*/ Repo repo) {
    //this.sessionProvider = sessionProvider;
    this.repo = repo;
  }

  private List<XmlOverallOfferStats> toStats(List<Object[]> dbResult) {
    List<XmlOverallOfferStats> result = newArrayList();
    for (Object[] record : dbResult) {

      long shows = SqlLoader.extractLong(record[0]);
      long clicks = SqlLoader.extractLong(record[1]);
      long leads = SqlLoader.extractLong(record[2]);
      long sales = SqlLoader.extractLong(record[3]);
      double confirmedRevenue = SqlLoader.extractDouble(record[4]);
      double notConfirmedRevenue = SqlLoader.extractDouble(record[5]);
      double canceledRevenue = SqlLoader.extractDouble(record[6]);
      long id = SqlLoader.extractLong(record[7]);
      String name = (String) record[8];
      double ctr = SqlLoader.extractDouble(record[9]);
      double cr = SqlLoader.extractDouble(record[10]);
      double ecpc = SqlLoader.extractDouble(record[11]);
      double ecpm = SqlLoader.extractDouble(record[12]);

      result.add(new XmlOverallOfferStats(id, name, shows, clicks, leads, sales,
          confirmedRevenue, notConfirmedRevenue, canceledRevenue, ctr, cr, ecpc, ecpm));
    }
    return result;
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> allOfferStats(boolean granted,
                                                           CommonParams commonParams) {
    Map<String, ?> templateParams = templateParamsBuilder(commonParams)
        .put("groupByOffer", true)
        .put("granted", granted).build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, commonParams);
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> affOfferStats(Long affiliateId,
                                                           CommonParams common) {
    Map<String, ?> templateParams = templateParamsBuilder(common)
        .put("groupByOffer", true)
        .put("filterByAffiliate", true).build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, common, ImmutableMap.of("aff_id", affiliateId));
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> advOfferStats(Long advertiserId,
                                                           CommonParams common) {
    Map<String, ?> templateParams = templateParamsBuilder(common)
        .put("groupByOffer", true)
        .put("addFee", true)
        .put("filterByAdvertiser", true).build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, common, ImmutableMap.of("adv_id", advertiserId));
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> affStats(CommonParams common) {

    Map<String, ?> templateParams = templateParamsBuilder(common)
        .put("groupByAffiliate", true).build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, common);
  }


  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> advStats(CommonParams common) {
    Map<String, ?> templateParams = templateParamsBuilder(common)
        .put("groupByAdvertiser", true)
        .put("addFee", true)
        .build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, common);
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> affStatsByOffer(
      long offerId, boolean forAdvertiser, CommonParams common) {

    ImmutableMap.Builder<String, Object> templateParams = templateParamsBuilder(common)
        .put("filterByOffer", true);
    if (forAdvertiser) {
      templateParams.put("groupByAffiliateId", true);
      templateParams.put("addFee", true);
    } else {
      templateParams.put("groupByAffiliate", true);
    }
    String sql = SqlLoader.getTemplate("offer_stats", templateParams.build());
    return executeStatsQuery(sql, common, ImmutableMap.of("offer_id", offerId));
  }


  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> sourceIdStats(Long affId,
                                                           Long offerId,
                                                           CommonParams common) {
    ImmutableMap.Builder<String, Object> templateParams = templateParamsBuilder(common);
    ImmutableMap.Builder<String, Object> queryParams = ImmutableMap.builder();
    if (affId != null) {
      templateParams.put("filterByAffiliate", true);
      queryParams.put("aff_id", affId);
    }
    if (offerId != null) {
      templateParams.put("filterByOffer", true);
      queryParams.put("offer_id", offerId);
    }
    templateParams.put("groupBySourceId", true);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams.build());

    return executeStatsQuery(sql, common, queryParams.build());
  }

  @Transactional
  public List<XmlOverallOfferStats> topAffiliates(DateTime from, DateTime to, int offset, int limit) {
    String sql =
        "select offer_stat.aff_id id, p.email e, sum(confirmed_revenue) r " +
            "from offer o " +
            "join offer_grant g on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to " +
            "  and o.id = offer_stat.master and g.aff_id = offer_stat.aff_id " +
            "left join user_profile p on offer_stat.aff_id = p.id " +
            "where o.parent_id is null and g.state = 'APPROVED' " +
            "group by offer_stat.aff_id, p.email " +
            "order by r desc, id asc " +
            "offset :offset limit :limit";

    Query query = repo.session().createSQLQuery(sql);
    @SuppressWarnings("unchecked")
    List<Object[]> dbResult = query
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list();

    List<XmlOverallOfferStats> result = newArrayList();
    for (Object[] record : dbResult) {
      long id = SqlLoader.extractLong(record[0]);
      if (id == 0L)
        continue;
      String name = (String) record[1];
      double confirmedRevenue = SqlLoader.extractDouble(record[2]);
      result.add(new XmlOverallOfferStats(id, name, confirmedRevenue));
    }
    return result;
  }


  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> subIdStats(
      Long affId, Long offerId, Map<String, String> filter,
      Set<String> grouping, CommonParams common) {

    // empty response if no grouping given
    if (grouping.size() == 0)
      return new Pair<List<XmlOverallOfferStats>, Long>(
          ImmutableList.<XmlOverallOfferStats>of(), 0L);

    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common);
    ImmutableMap.Builder<String, Object> queryParams = ImmutableMap.builder();
    if (affId != null) {
      templateParams.put("filterByAffiliate", true);
      queryParams.put("aff_id", affId);
    }
    if (offerId != null) {
      templateParams.put("filterByOffer", true);
      queryParams.put("offer_id", offerId);
    }
    templateParams.put("groupBySub", grouping);
    templateParams.put("filterBySub", filter.keySet());
    queryParams.putAll(filter);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams.build());
    return executeStatsQuery(sql, common, queryParams.build());
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> refererStats(
      Long affId, Long offerId, CommonParams common) {
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common);
    ImmutableMap.Builder<String, Object> queryParams = ImmutableMap.builder();
    if (affId != null) {
      templateParams.put("filterByAffiliate", true);
      queryParams.put("aff_id", affId);
    }
    if (offerId != null) {
      templateParams.put("filterByOffer", true);
      queryParams.put("offer_id", offerId);
    }
    templateParams.put("groupByReferer", true);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams.build());
    return executeStatsQuery(sql, common, queryParams.build());
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> keywordsStats(
      Long affId, Long offerId, CommonParams common) {

    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common);
    ImmutableMap.Builder<String, Object> queryParams = ImmutableMap.builder();
    if (affId != null) {
      templateParams.put("filterByAffiliate", true);
      queryParams.put("aff_id", affId);
    }
    if (offerId != null) {
      templateParams.put("filterByOffer", true);
      queryParams.put("offer_id", offerId);
    }
    templateParams.put("groupByKeywords", true);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams.build());
    return executeStatsQuery(sql, common, queryParams.build());
  }

  @SuppressWarnings("unchecked")
  public Pair<QueryResult, Long> subofferStatForOffer(Long affId,
                                                      Long offerId,
                                                      CommonParams common) {
    Preconditions.checkNotNull(offerId, "Offer id should not be null.");
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
        .put("filterByParentId", true);
    ImmutableMap.Builder<String, Object> queryParams =
        ImmutableMap.<String, Object>builder()
        .put("parent_id", offerId);
    if (affId != null) {
      templateParams.put("filterByAffId", true);
      queryParams.put("aff_id", affId);
    }
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams.build());
  }

  public Pair<QueryResult, Long> subofferStatForAffiliate(Long affId,
                                                          CommonParams common) {
    Preconditions.checkNotNull(affId, "Affiliate id should not be null");
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
        .put("filterByAffId", true);
    ImmutableMap<String, ?> queryParams = ImmutableMap.of("aff_id", affId);
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams);
  }

  public Pair<QueryResult, Long> subofferStatForAdvertiser(Long advId,
                                                           CommonParams common) {
    Preconditions.checkNotNull(advId, "Advertiser id should not be null");
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
            .put("filterByAdvId", true);
    ImmutableMap<String, ?> queryParams = ImmutableMap.of("adv_id", advId);
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams);
  }

  public Pair<QueryResult, Long> subofferStatForSubIds(Long affId, Long offerId,
                                                       ImmutableMap<String, String> subIdFilter,
                                                       CommonParams common) {
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
            .put("filterBySubId", subIdFilter.keySet())
            .put("filterByAffId", true);
    ImmutableMap.Builder<String, Object> queryParams =
        ImmutableMap.<String, Object>builder()
            .put("aff_id", affId)
            .putAll(subIdFilter);
    if (offerId != null) {
      templateParams.put("filterByParentId", true);
      queryParams.put("parent_id", offerId);
    }
    if (subIdFilter.isEmpty()) {
      templateParams.put("emptySubIdsOnly", true);
    }
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams.build());

  }


  public Pair<QueryResult, Long> subofferStatForSourceId(Long affId,
                                                         Long offerId,
                                                         String sourceId,
                                                         CommonParams common) {
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
        .put("filterBySourceId", true)
        .put("filterByAffId", true);
    if (offerId != null)
      templateParams.put("filterByParentId", true);
    ImmutableMap.Builder<String, Object> queryParams =
        ImmutableMap.<String, Object>builder()
        .put("source_id", sourceId)
        .put("aff_id", affId);
    if (offerId != null)
      queryParams.put("parent_id", offerId);
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams.build());
  }

  public Pair<QueryResult, Long> subofferStatForReferer(Long affId,
                                                        Long offerId,
                                                        String referer,
                                                        CommonParams common) {
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
        .put("filterByReferer", true);
    ImmutableMap.Builder<String, Object> queryParams =
        ImmutableMap.<String, Object>builder()
            .put("referer", referer);
    if (offerId != null) {
      templateParams.put("filterByParentId", true);
      queryParams.put("parent_id", offerId);
    }
    if (affId != null) {
      templateParams.put("filterByAffId", true);
      queryParams.put("aff_id", affId);
    }
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams.build());
  }

  public Pair<QueryResult, Long> subofferStatForKeywords(Long affId,
                                                         Long offerId,
                                                         String keywords,
                                                         CommonParams common) {
    ImmutableMap.Builder<String, Object> templateParams =
        templateParamsBuilder(common)
        .put("filterByKeywords", true);
    ImmutableMap.Builder<String, Object> queryParams =
        ImmutableMap.<String, Object>builder()
            .put("keywords", keywords);
    if (offerId != null) {
      templateParams.put("filterByParentId", true);
      queryParams.put("parent_id", offerId);
    }
    if (affId != null) {
      templateParams.put("filterByAffId", true);
      queryParams.put("aff_id", affId);
    }
    String sql = SqlLoader.getTemplate("suboffer_stats", templateParams.build());
    return executeSubOfferStatsQuery(sql, common, queryParams.build());
  }



  @SuppressWarnings("unchecked")
  public Map<String, BigDecimal> totalStats(DateTime from, DateTime to) {
    BigDecimal canceledFee = sumAllEntries(
        "adminNotConfirmedMoney",
        OfferActionState.CANCELED,
        AccountingEvent.ACTION_CANCELED, from, to).negate();
    BigDecimal canceledAffiliate = sumAllEntries(
        "affiliateNotConfirmedMoney",
        OfferActionState.CANCELED,
        AccountingEvent.ACTION_CANCELED, from, to).negate();
    BigDecimal notConfirmedFee = sumAllEntries(
        "adminNotConfirmedMoney",
        OfferActionState.NOT_APPROVED,
        AccountingEvent.ACTION_CREATED, from, to);
    BigDecimal notConfirmedAff = sumAllEntries(
        "affiliateNotConfirmedMoney",
        OfferActionState.NOT_APPROVED,
        AccountingEvent.ACTION_CREATED, from, to);
    BigDecimal confirmedFee = sumAllEntries(
        "adminConfirmedMoney",
        OfferActionState.APPROVED,
        AccountingEvent.ACTION_APPROVED, from, to);
    BigDecimal confirmedAff = sumAllEntries(
        "affiliateConfirmedMoney",
        OfferActionState.APPROVED,
        AccountingEvent.ACTION_APPROVED, from, to);
    BigDecimal expiredFee = sumExpiredEntries(
        "adminNotConfirmedMoney", from, to);
    BigDecimal expiredAffiliate = sumExpiredEntries(
        "affiliateNotConfirmedMoney", from, to);
    return ImmutableMap.<String, BigDecimal>builder()
        .put(CANCELED_FEE, canceledFee)
        .put(CANCELED_AFFILIATE, canceledAffiliate)
        .put(CANCELED_SUM, canceledFee.add(canceledAffiliate))
        .put(NOT_CONFIRMED_FEE, notConfirmedFee)
        .put(NOT_CONFIRMED_AFFILIATE, notConfirmedAff)
        .put(NOT_CONFIRMED_SUM, notConfirmedFee.add(notConfirmedAff))
        .put(CONFIRMED_FEE, confirmedFee)
        .put(CONFIRMED_AFFILIATE, confirmedAff)
        .put(CONFIRMED_SUM, confirmedFee.add(confirmedAff))
        .put(EXPIRED_AFFILIATE, expiredAffiliate)
        .put(EXPIRED_FEE, expiredFee)
        .put(EXPIRED_SUM, expiredFee.add(expiredAffiliate))
        .build();
  }

  private Object[] totalStats(OfferActionState actionState,
                              AccountingEvent event,
                              DateTime from, DateTime to) {

    String totalQuery = SqlLoader.getSql("total_stat");
    return (Object[]) repo.session()
        .createSQLQuery(totalQuery)
        .setParameter("action_state", actionState.ordinal())
        .setParameter("entry_event", event.ordinal())
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .list().get(0);
  }

  private BigDecimal sumAllEntries(String templateFlag,
                                   OfferActionState state,
                                   AccountingEvent event,
                                   DateTime from, DateTime to) {
    String sql = SqlLoader.getTemplate("total_stat",
        ImmutableMap.of(templateFlag, true));
    Query query = repo.session().createSQLQuery(sql)
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .setParameter("entry_event", event.ordinal())
        .setParameter("action_state", state.ordinal());
    return SqlLoader.scaledDecimal(query.uniqueResult());
  }

  private BigDecimal sumExpiredEntries(String templateFlag,
                                       DateTime from, DateTime to) {
    String sql = SqlLoader.getTemplate(
        "total_stat",
        ImmutableMap.of(templateFlag, true, "onlyExpiredActions", true));
    Query query = repo.session().createSQLQuery(sql)
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate());
    return SqlLoader.scaledDecimal(query.uniqueResult());
  }


  private Pair<List<XmlOverallOfferStats>, Long> executeStatsQuery(String sql, CommonParams common) {
    return executeStatsQuery(sql, common, ImmutableMap.<String, Object>of());
  }

  private Pair<List<XmlOverallOfferStats>, Long> executeStatsQuery(
      String sql, CommonParams common, Map<String, ?> custom) {

    Long count = count(sql, common, custom);
    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    for (Map.Entry<String, ?> parameter : custom.entrySet()) {
      query.setParameter(parameter.getKey(), parameter.getValue());
    }
    @SuppressWarnings("unchecked")
    List<XmlOverallOfferStats> result = toStats(query
        .setTimestamp("from", common.from.toDate())
        .setTimestamp("to", common.to.toDate())
        .setParameter("offset", common.offset)
        .setParameter("limit", common.limit)
        .list());
    return new Pair<List<XmlOverallOfferStats>, Long>(result, count);
  }

  private Pair<QueryResult, Long> executeSubOfferStatsQuery(String sql,
                                                            CommonParams common,
                                                            Map<String, ?> custom) {
    Long count = count(sql, common, custom);
    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    for (Map.Entry<String, ?> parameter : custom.entrySet()) {
      query.setParameter(parameter.getKey(), parameter.getValue());
    }
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> result = query
        .setTimestamp("from", common.from.toDate())
        .setTimestamp("to", common.to.toDate())
        .setParameter("offset", common.offset)
        .setParameter("limit", common.limit)
        .setResultTransformer(ImmutableMapTransformer.INSTANCE)
        .list();
    return new Pair<QueryResult, Long>(new QueryResult(result), count);
  }

  private Long count(String sql, CommonParams common, Map<String, ?> custom) {
    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(SqlLoader.countSql(sql));
    for (Map.Entry<String, ?> parameter : custom.entrySet()) {
      countQuery.setParameter(parameter.getKey(), parameter.getValue());
    }
    return SqlLoader.extractLong(countQuery
        .setTimestamp("from", common.from.toDate())
        .setTimestamp("to", common.to.toDate())
        .uniqueResult()
    );
  }

  private static ImmutableMap.Builder<String, Object> templateParamsBuilder(
      CommonParams common) {
    return ImmutableMap.<String, Object>builder()
        .put("ordering", common.ordering)
        .put("direction", common.direction);
  }
}
