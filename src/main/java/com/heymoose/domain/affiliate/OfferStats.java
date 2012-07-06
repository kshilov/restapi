package com.heymoose.domain.affiliate;

import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.Pair;
import com.heymoose.util.SqlLoader;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class OfferStats {

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
  public static final String EXPIRED_FEE ="expired_fee";
  public static final String EXPIRED_SUM = "expired_sum";

  //private final Provider<Session> sessionProvider;
  private final Repo repo;

  @Inject
  public OfferStats(/*Provider<Session> sessionProvider,*/ Repo repo) {
    //this.sessionProvider = sessionProvider;
    this.repo = repo;
  }

  private List<OverallOfferStats> toStats(List<Object[]> dbResult) {
    List<OverallOfferStats> result = newArrayList();
    for (Object[] record : dbResult) {

      long shows = extractLong(record[0]);
      long clicks = extractLong(record[1]);
      long leads = extractLong(record[2]);
      long sales = extractLong(record[3]);
      double confirmedRevenue = extractDouble(record[4]);
      double notConfirmedRevenue = extractDouble(record[5]);
      double canceledRevenue = extractDouble(record[6]);
      long id = extractLong(record[7]);
      String name = (String) record[8];
      double ctr = extractDouble(record[9]);
      double cr = extractDouble(record[10]);
      double ecpc = extractDouble(record[11]);
      double ecpm = extractDouble(record[12]);

      result.add(new OverallOfferStats(id, name, shows, clicks, leads, sales,
          confirmedRevenue, notConfirmedRevenue, canceledRevenue, ctr, cr, ecpc, ecpm));
    }
    return result;
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> allOfferStats(boolean granted,
                                                           DateTime from,
                                                           DateTime to,
                                                           int offset,
                                                           int limit) {
    Map<String, ?> templateParams = ImmutableMap.of(
            "groupByOffer", true,
            "granted", granted);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, from, to, offset, limit,
        ImmutableMap.<String, Object>of());
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> affOfferStats(Long affiliateId,
                                                           DateTime from,
                                                           DateTime to,
                                                           int offset,
                                                           int limit) {
    Map<String, ?> templateParams = ImmutableMap.of(
            "groupByOffer", true,
            "filterByAffiliate", true);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, from, to, offset, limit,
        ImmutableMap.<String, Object>of("aff_id", affiliateId));
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> advOfferStats(Long advertiserId,
                                                           DateTime from,
                                                           DateTime to,
                                                           int offset,
                                                           int limit) {
    Map<String, ?> templateParams = ImmutableMap.of(
            "groupByOffer", true,
            "filterByAdvertiser", true);
    String sql = SqlLoader.getTemplate("offer_stats", templateParams);
    return executeStatsQuery(sql, from, to, offset, limit,
        ImmutableMap.<String, Object>of("adv_id", advertiserId));
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> affStats(DateTime from, DateTime to,
                                                      int offset, int limit) {

    String sql = SqlLoader.getTemplate(
        "offer_stats",
        ImmutableMap.of("groupByAffiliate", true));
    return executeStatsQuery(sql, from, to, offset, limit);
  }


  @Transactional
  public Pair<List<OverallOfferStats>, Long> advStats(DateTime from, DateTime to,
                                                      int offset, int limit) {
    String sql = SqlLoader.getTemplate(
        "offer_stats",
        ImmutableMap.of("groupByAdvertiser", true));
    return executeStatsQuery(sql, from, to, offset, limit);
  }


  private Pair<List<OverallOfferStats>, Long> executeStatsQuery(String sql,
                                                                DateTime from, DateTime to,
                                                                int offset, int limit) {
    return executeStatsQuery(sql, from, to, offset, limit,
        ImmutableMap.<String, Object>of());
  }

  private Pair<List<OverallOfferStats>, Long> executeStatsQuery(String sql,
                                                                DateTime from, DateTime to,
                                                                int offset, int limit,
                                                                Map<String, ?> parameterMap) {

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    for (Map.Entry<String, ?> parameter : parameterMap.entrySet()) {
      countQuery.setParameter(parameter.getKey(), parameter.getValue());
    }
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    for (Map.Entry<String, ?> parameter : parameterMap.entrySet()) {
      query.setParameter(parameter.getKey(), parameter.getValue());
    }
    @SuppressWarnings("unchecked")
    List<OverallOfferStats> result = toStats(query
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list());
    return new Pair<List<OverallOfferStats>, Long>(result, count);
  }

  private String nowSql() {
    return DateTime.now().toString("YYYY-MM-dd HH:mm:ss");
  }

  @Transactional
  public List<OverallOfferStats> topAffiliates(DateTime from, DateTime to, int offset, int limit) {
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

    List<OverallOfferStats> result = newArrayList();
    for (Object[] record : dbResult) {
      long id = extractLong(record[0]);
      if (id == 0L)
        continue;
      String name = (String) record[1];
      double confirmedRevenue = extractDouble(record[2]);
      result.add(new OverallOfferStats(id, name, confirmedRevenue));
    }
    return result;
  }

  @Transactional
  @SuppressWarnings("unchecked")
  public Pair<List<OverallOfferStats>, Long> affStatsByOffer(
      boolean granted, long offerId, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a2";
    String groupBy = "offer_stat.aff_id, p.email";
    String select = "offer_stat.aff_id a8, p.email a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "left join user_profile p on offer_stat.aff_id = p.id " +
        "where o.parent_id is null and o.id = :offer " +
        (granted ? "and g.state = 'APPROVED' " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    Long count = extractLong(countQuery
        .setParameter("offer", offerId)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    @SuppressWarnings("unchecked")
    List<OverallOfferStats> stats = toStats(query
        .setParameter("offer", offerId)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list()
    );
    return new Pair<List<OverallOfferStats>, Long>(stats, count);
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> sourceIdStats(
      boolean granted, Long affId, Long offerId, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a9";
    String groupBy = "offer_stat.source_id";
    String select = "0 a8, offer_stat.source_id a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "where o.parent_id is null " +
        (granted ? "and g.state = 'APPROVED' " : "") +
        (affId != null ? "and offer_stat.aff_id = :affId " : "") +
        (offerId != null ? "and o.id = :offerId " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    if (affId != null) countQuery.setParameter("affId", affId);
    if (offerId != null) countQuery.setParameter("offerId", offerId);
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    if (affId != null) query.setParameter("affId", affId);
    if (offerId != null) query.setParameter("offerId", offerId);
    @SuppressWarnings("unchecked")
    List<OverallOfferStats> stats = toStats(query
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list()
    );
    return new Pair<List<OverallOfferStats>, Long>(stats, count);
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> subIdStats(
      boolean granted, Long affId, Long offerId, List<String> subsFilter,
      List<Boolean> grouping, DateTime from, DateTime to, int offset, int limit) {

    // filter
    String filter = "";
    for (int i = 0; i < subsFilter.size(); i++) {
      if (subsFilter.get(i) != null) {
        filter += "and sub_id" + (i == 0 ? "" : i) + " = :sub_id" + i + " ";
        grouping.set(i, false); // do not group where value is filtered
      }
    }

    // grouping
    String[] subs = new String[5];
    int j = 0;
    for (int i = 0; i < grouping.size(); i++) {
      if (grouping.get(i)) {
        subs[j] = "offer_stat.sub_id" + (i == 0 ? "" : i);
        j++;
      }
    }

    // at least one grouping of not-filtered values should exist
    if (j == 0) return new Pair<List<OverallOfferStats>, Long>(new ArrayList<OverallOfferStats>(), 0L);

    // clauses
    String orderBy = "a9";
    String groupBy = StringUtils.join(subs, ", ", 0, j);
    String select = (j == 1) ? "0 a8, " + subs[0] + " a9"
        : "0 a8, concat(" + StringUtils.join(subs, ", ' / ', ", 0, j) + ") a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "where o.parent_id is null " +
        filter +
        (granted ? "and g.state = 'APPROVED' " : "") +
        (affId != null ? "and offer_stat.aff_id = :affId " : "") +
        (offerId != null ? "and o.id = :offerId " : "") +
        "group by " + groupBy + " order by " + orderBy + " offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    if (affId != null) countQuery.setParameter("affId", affId);
    if (offerId != null) countQuery.setParameter("offerId", offerId);
    addSubsParametersToQuery(subsFilter, countQuery);
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    if (affId != null) query.setParameter("affId", affId);
    if (offerId != null) query.setParameter("offerId", offerId);
    addSubsParametersToQuery(subsFilter, query);
    @SuppressWarnings("unchecked")
    List<OverallOfferStats> stats = toStats(query
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list()
    );
    return new Pair<List<OverallOfferStats>, Long>(stats, count);
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> refererStats(
      boolean granted, Long affId, Long offerId, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a9";
    String groupBy = "offer_stat.referer";
    String select = "0 a8, offer_stat.referer a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "where o.parent_id is null " +
        (granted ? "and g.state = 'APPROVED' " : "") +
        (affId != null ? "and offer_stat.aff_id = :affId " : "") +
        (offerId != null ? "and o.id = :offerId " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    if (affId != null) countQuery.setParameter("affId", affId);
    if (offerId != null) countQuery.setParameter("offerId", offerId);
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    if (affId != null) query.setParameter("affId", affId);
    if (offerId != null) query.setParameter("offerId", offerId);
    @SuppressWarnings("unchecked")
    List<OverallOfferStats> stats = toStats(query
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list()
    );
    return new Pair<List<OverallOfferStats>, Long>(stats, count);
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> keywordsStats(
      boolean granted, Long affId, Long offerId, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a9";
    String groupBy = "offer_stat.keywords";
    String select = "0 a8, offer_stat.keywords a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "where o.parent_id is null " +
        (granted ? "and g.state = 'APPROVED' " : "") +
        (affId != null ? "and offer_stat.aff_id = :affId " : "") +
        (offerId != null ? "and o.id = :offerId " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    if (affId != null) countQuery.setParameter("affId", affId);
    if (offerId != null) countQuery.setParameter("offerId", offerId);
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    if (affId != null) query.setParameter("affId", affId);
    if (offerId != null) query.setParameter("offerId", offerId);
    @SuppressWarnings("unchecked")
    List<OverallOfferStats> stats = toStats(query
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list()
    );
    return new Pair<List<OverallOfferStats>, Long>(stats, count);
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
      AccountingEvent.ACTION_APPROVED, from ,to);

    Object[] expiredQueryResult = (Object[]) repo.session()
        .createSQLQuery(SqlLoader.get(expiredQuery))
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .list().get(0);

    return ImmutableMap.<String, BigDecimal>builder()
        .put(CANCELED_FEE, scaledDecimal(canceled[0]).negate())
        .put(CANCELED_AFFILIATE, scaledDecimal(canceled[1]).negate())
        .put(CANCELED_SUM, scaledDecimal(canceled[2]).negate())
        .put(NOT_CONFIRMED_FEE, scaledDecimal(notConfirmed[0]))
        .put(NOT_CONFIRMED_AFFILIATE, scaledDecimal(notConfirmed[1]))
        .put(NOT_CONFIRMED_SUM, scaledDecimal(notConfirmed[2]))
        .put(CONFIRMED_FEE, scaledDecimal(confirmed[0]).negate())
        .put(CONFIRMED_AFFILIATE, scaledDecimal(confirmed[1]).negate())
        .put(CONFIRMED_SUM, scaledDecimal(confirmed[2]).negate())
        .put(EXPIRED_AFFILIATE, scaledDecimal(expiredQueryResult[0]))
        .put(EXPIRED_FEE, scaledDecimal(expiredQueryResult[1]))
        .put(EXPIRED_SUM, scaledDecimal(expiredQueryResult[2]))
        .build();
  }

  private Object[] totalStats(OfferActionState actionState,
                              AccountingEvent event,
                              DateTime from, DateTime to) {

    String totalQuery = SqlLoader.get("total_stat");
    return (Object[]) repo.session()
        .createSQLQuery(totalQuery)
        .setParameter("action_state", actionState.ordinal())
        .setParameter("entry_event", event.ordinal())
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .list().get(0);
  }
  private static Long extractLong(Object val) {
    if (val == null)
      return 0L;
    if (val instanceof BigInteger)
      return ((BigInteger) val).longValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).longValue();
    if (val instanceof Integer)
      return ((Integer) val).longValue();
    throw new IllegalStateException();
  }

  private static BigDecimal scaledDecimal(Object object) {
    if (object == null)
      return new BigDecimal("0.00");
    BigDecimal decimal = (BigDecimal) object;
    return decimal.setScale(2, BigDecimal.ROUND_UP);
  }

  private static double extractDouble(Object val) {
    if (val == null)
      return 0.0;
    if (val instanceof BigInteger)
      return ((BigInteger) val).doubleValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).doubleValue();
    throw new IllegalStateException();
  }

  private void addSubsParametersToQuery(List<String> subs, Query query) {
    for (int i = 0; i < subs.size(); i++) {
      if (subs.get(i) != null) {
        query.setParameter("sub_id" + i, subs.get(i));
      }
    }
  }

  private String countSql(String sql) {
    sql = sql.replaceFirst("select .* from ", "select count(*) from ");
    sql = sql.substring(0, sql.lastIndexOf("order by"));
    return "select count(*) from (" + sql + ") c";
  }
}
