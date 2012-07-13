package com.heymoose.domain.affiliate.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.OfferActionState;
import com.heymoose.resource.xml.XmlOverallOfferStats;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.OrderingDirection;
import com.heymoose.util.Pair;
import com.heymoose.util.SqlLoader;
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
        .put("groupByAdvertiser", true).build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, common);
  }

  @Transactional
  public Pair<List<XmlOverallOfferStats>, Long> affStatsByOffer(
      long offerId, CommonParams common) {

    Map<String, ?> templateParams = templateParamsBuilder(common)
        .put("groupByAffiliate", true)
        .put("filterByOffer", true).build();
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
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
  public Map<String, BigDecimal> totalStats(DateTime from, DateTime to) {
    String expiredQuery = "expired_actions_stat";

    Object[] canceled = totalStats(
        OfferActionState.CANCELED,
        AccountingEvent.ACTION_CANCELED, from, to);

    Object[] notConfirmed = totalStats(
        OfferActionState.NOT_APPROVED,
        AccountingEvent.ACTION_CREATED, from, to);

    Object[] confirmed = totalStats(
        OfferActionState.APPROVED,
        AccountingEvent.ACTION_APPROVED, from, to);

    Object[] expiredQueryResult = (Object[]) repo.session()
        .createSQLQuery(SqlLoader.getSql(expiredQuery))
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .list().get(0);

    return ImmutableMap.<String, BigDecimal>builder()
        .put(CANCELED_FEE, SqlLoader.scaledDecimal(canceled[0]).negate())
        .put(CANCELED_AFFILIATE, SqlLoader.scaledDecimal(canceled[1]).negate())
        .put(CANCELED_SUM, SqlLoader.scaledDecimal(canceled[2]).negate())
        .put(NOT_CONFIRMED_FEE, SqlLoader.scaledDecimal(notConfirmed[0]))
        .put(NOT_CONFIRMED_AFFILIATE, SqlLoader.scaledDecimal(notConfirmed[1]))
        .put(NOT_CONFIRMED_SUM, SqlLoader.scaledDecimal(notConfirmed[2]))
        .put(CONFIRMED_FEE, SqlLoader.scaledDecimal(confirmed[0]).negate())
        .put(CONFIRMED_AFFILIATE, SqlLoader.scaledDecimal(confirmed[1]).negate())
        .put(CONFIRMED_SUM, SqlLoader.scaledDecimal(confirmed[2]).negate())
        .put(EXPIRED_AFFILIATE, SqlLoader.scaledDecimal(expiredQueryResult[0]))
        .put(EXPIRED_FEE, SqlLoader.scaledDecimal(expiredQueryResult[1]))
        .put(EXPIRED_SUM, SqlLoader.scaledDecimal(expiredQueryResult[2]))
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


  private Pair<List<XmlOverallOfferStats>, Long> executeStatsQuery(String sql, CommonParams common) {
    return executeStatsQuery(sql, common, ImmutableMap.<String, Object>of());
  }

  private Pair<List<XmlOverallOfferStats>, Long> executeStatsQuery(
      String sql, CommonParams common, Map<String, ?> custom) {

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(SqlLoader.countSql(sql));
    for (Map.Entry<String, ?> parameter : custom.entrySet()) {
      countQuery.setParameter(parameter.getKey(), parameter.getValue());
    }
    Long count = SqlLoader.extractLong(countQuery
        .setTimestamp("from", common.from.toDate())
        .setTimestamp("to", common.to.toDate())
        .uniqueResult()
    );

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

  private static ImmutableMap.Builder<String, Object> templateParamsBuilder(
      CommonParams common) {
    return ImmutableMap.<String, Object>builder()
        .put("ordering", common.ordering)
        .put("direction", common.direction);
  }
}
