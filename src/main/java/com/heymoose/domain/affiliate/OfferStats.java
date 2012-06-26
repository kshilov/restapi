package com.heymoose.domain.affiliate;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.joda.time.DateTime;

@Singleton
public class OfferStats {

  private final Repo repo;

  @Inject
  public OfferStats(Repo repo) {
    this.repo = repo;
  }

  private List<OverallOfferStats> toStats(List<Object[]> dbResult) {
    List<OverallOfferStats> result = newArrayList();
    for (Object[] record : dbResult) {
      // a1 - a7
      long shows = extractLong(record[0]);
      long clicks = extractLong(record[1]);
      long leads = extractLong(record[2]);
      long sales = extractLong(record[3]);
      double confirmedRevenue = extractDouble(record[4]);
      double notConfirmedRevenue = extractDouble(record[5]);
      double canceledRevenue = extractDouble(record[6]);
      // calculations
      Double ctr = (shows == 0) ? null : clicks * 100.0 / shows;
      Double cr = (clicks == 0) ? null : (leads + sales) * 100.0 / clicks;
      Double ecpc = (clicks == 0) ? null : (confirmedRevenue + notConfirmedRevenue) / clicks;
      Double ecpm = (shows == 0) ? null : (confirmedRevenue + notConfirmedRevenue) * 1000.0 / shows;

      long offerId = extractLong(record[7]); // a8
      String name = (String) record[8]; // a9

      result.add(new OverallOfferStats(offerId, name, shows, clicks, leads, sales,
          confirmedRevenue, notConfirmedRevenue, canceledRevenue, ctr, cr, ecpc, ecpm));
    }
    return result;
  }

  @Transactional
  public Pair<List<OverallOfferStats>, Long> offerStats(
      boolean granted, Long affId, Long advId, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a2";
    String groupBy = "o.id, o.name";
    String select = "o.id a8, o.name a9";
    // particular
    if (affId != null && granted) {
      orderBy = "g.creation_time";
      groupBy = "o.id, o.name, g.creation_time";
    }
    if (advId != null) {
      orderBy = "a8";
    }

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
        (advId != null ? "and o.user_id = :advId " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    if (affId != null) countQuery.setParameter("affId", affId);
    if (advId != null) countQuery.setParameter("advId", advId);
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    if (affId != null) query.setParameter("affId", affId);
    if (advId != null) query.setParameter("advId", advId);
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
  public Pair<List<OverallOfferStats>, Long> affStats(boolean granted, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a2";
    String groupBy = "offer_stat.aff_id, p.first_name, p.last_name";
    String select = "offer_stat.aff_id a8, p.first_name || ' ' || p.last_name a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "left join user_profile p on offer_stat.aff_id = p.id " +
        "where o.parent_id is null " +
        (granted ? "and g.state = 'APPROVED' " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
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
  public Pair<List<OverallOfferStats>, Long> advStats(boolean expired, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a2";
    String groupBy = "p.id, p.first_name, p.last_name, p.organization";
    String select = "p.id a8, p.first_name || ' ' || p.last_name || ' (' || coalesce(p.organization, '--') || ')' a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        "left join offer_stat on offer_stat.creation_time between :from and :to and o.id = offer_stat.master " +
        "left join user_profile p on o.user_id = p.id " +
        (expired ? "left join offer_action oa on oa.stat_id = offer_stat.id " : "") +
        "where o.parent_id is null " +
        (expired ? "and cast(now() as date) - cast(oa.creation_time as date) > o.hold_days " : "") +
        "group by " + groupBy + " order by " + orderBy + " desc offset :offset limit :limit";

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(countSql(sql));
    Long count = extractLong(countQuery
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .uniqueResult()
    );

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
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

  private String nowSql() {
    return DateTime.now().toString("YYYY-MM-dd HH:mm:ss");
  }

  @Transactional
  public List<OverallOfferStats> topAffiliates(DateTime from, DateTime to, int offset, int limit) {
    String sql =
        "select offer_stat.aff_id id, p.first_name || ' ' || p.last_name n, sum(confirmed_revenue) r " +
            "from offer o " +
            "join offer_grant g on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to " +
            "  and o.id = offer_stat.master and g.aff_id = offer_stat.aff_id " +
            "left join user_profile p on offer_stat.aff_id = p.id " +
            "where o.parent_id is null and g.state = 'APPROVED' " +
            "group by offer_stat.aff_id, p.first_name, p.last_name " +
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
    String groupBy = "offer_stat.aff_id, p.first_name, p.last_name";
    String select = "offer_stat.aff_id a8, p.first_name || ' ' || p.last_name a9";

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
      }
    }

    // grouping
    String[] subs = new String[5];
    int j = 0;
    for (int i = 0; i < grouping.size(); i++) {
      if (grouping.get(i)) {
        subs[j++] = "offer_stat.sub_id" + (i == 0 ? "" : i);
      }
    }

    // at least one grouping or one filtered values should exist
    if (j == 0 && "".equals(filter))
      return new Pair<List<OverallOfferStats>, Long>(new ArrayList<OverallOfferStats>(), 0L);

    // clauses
    String orderBy, groupBy, select;
    if (j == 0) {
      orderBy = "a9";
      groupBy = null;
      select = "show_count a1, coalesce(click_count, 0) a2, " +
          "leads_count a3, sales_count a4, confirmed_revenue a5, " +
          "not_confirmed_revenue a6, canceled_revenue a7, " +
          "0 a8, concat(offer_stat.sub_id, ' / ', offer_stat.sub_id1, ' / ', " +
          "offer_stat.sub_id2, ' / ', offer_stat.sub_id3, ' / ', offer_stat.sub_id4) a9";
    } else {
      orderBy = "a9";
      groupBy = StringUtils.join(subs, ", ", 0, j);
      select = "sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
          "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
          "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " +
          (j == 1 ?
              "0 a8, " + subs[0] + " a9"
              : "0 a8, concat(" + StringUtils.join(subs, ", ' / ', ", 0, j) + ") a9");
    }

    // sql
    String sql = "select " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "where o.parent_id is null " +
        filter +
        (granted ? "and g.state = 'APPROVED' " : "") +
        (affId != null ? "and offer_stat.aff_id = :affId " : "") +
        (offerId != null ? "and o.id = :offerId " : "") +
        (groupBy != null ? "group by " + groupBy + " " : "") +
        "order by " + orderBy + " offset :offset limit :limit";

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
    sql = sql.substring(0, sql.lastIndexOf(" order by "));
    return "select count(*) from (" + sql + ") c";
  }
}
