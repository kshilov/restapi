package com.heymoose.domain.affiliate;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;

@Singleton
public class OfferStats {

  private final Provider<Session> sessionProvider;
  private final Repo repo;

  @Inject
  public OfferStats(Provider<Session> sessionProvider, Repo repo) {
    this.sessionProvider = sessionProvider;
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
      Double ctr = (shows == 0) ? null : clicks * 100.0 / shows;
      Double cr = (clicks == 0) ? null : (leads + sales) * 100.0 / clicks;
      Double ecpc = (clicks == 0) ? null : (confirmedRevenue + notConfirmedRevenue) / clicks;
      Double ecpm = (shows == 0) ? null : (confirmedRevenue + notConfirmedRevenue) * 1000.0 / shows;

      long offerId = extractLong(record[7]);
      String name = (String) record[8];

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
    if (affId != null) {
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
        (affId != null ? "and g.aff_id = :affId " : "") +
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
    String groupBy = "g.aff_id, p.first_name, p.last_name";
    String select = "g.aff_id a8, p.first_name || ' ' || p.last_name a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "left join user_profile p on g.aff_id = p.id " +
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
  @SuppressWarnings("unchecked")
  public Pair<List<OverallOfferStats>, Long> affStatsByOffer(
      long offerId, boolean granted, DateTime from, DateTime to, int offset, int limit) {

    // default
    String orderBy = "a2";
    String groupBy = "g.aff_id, p.first_name, p.last_name";
    String select = "g.aff_id a8, p.first_name || ' ' || p.last_name a9";

    // sql
    String sql = "select sum(show_count) a1, sum(coalesce(click_count, 0)) a2, " +
        "sum(leads_count) a3, sum(sales_count) a4, sum(confirmed_revenue) a5, " +
        "sum(not_confirmed_revenue) a6, sum(canceled_revenue) a7, " + select + " from offer o " +
        (granted ? "join offer_grant g on g.offer_id = o.id " : "") +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and o.id = offer_stat.master " +
        (granted ? "and g.aff_id = offer_stat.aff_id " : "") +
        "left join user_profile p on g.aff_id = p.id " +
        "where o.parent_id is null and g.offer_id = :offer " +
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

/*  private void addSubsParametersToQuery(Subs subs, Query dbQuery) {
    if (subs.sourceId() != null) dbQuery.setParameter("source_id", subs.sourceId());
    if (subs.subId() != null) dbQuery.setParameter("sub_id", subs.subId());
    if (subs.subId1() != null) dbQuery.setParameter("sub_id1", subs.subId1());
    if (subs.subId2() != null) dbQuery.setParameter("sub_id2", subs.subId2());
    if (subs.subId3() != null) dbQuery.setParameter("sub_id3", subs.subId3());
    if (subs.subId4() != null) dbQuery.setParameter("sub_id4", subs.subId4());
  }

  private String addSubsToSql(Subs subs) {
    String query = "";
    if (subs.sourceId() != null) query += "and offer_stat.source_id = :source_id ";
    if (subs.subId() != null) query += "and offer_stat.sub_id = :sub_id ";
    if (subs.subId1() != null) query += "and offer_stat.sub_id1 = :sub_id1 ";
    if (subs.subId2() != null) query += "and offer_stat.sub_id2 = :sub_id2 ";
    if (subs.subId3() != null) query += "and offer_stat.sub_id3 = :sub_id3 ";
    if (subs.subId4() != null) query += "and offer_stat.sub_id4 = :sub_id4 ";
    return query;
  }

  private String addSubGroupToSqlInSelect(String subGroup) {
    String g = addSubGroupToSqlInGroupBy(subGroup);
    if ("".equals(g)) return g;
    else return g + " s";
  }

  private String addSubGroupToSqlInGroupBy(String subGroup) {
    if ("source_id".equals(subGroup)) return ", source_id";
    if ("sub_id".equals(subGroup)) return ", sub_id";
    if ("sub_id1".equals(subGroup)) return ", sub_id1";
    if ("sub_id2".equals(subGroup)) return ", sub_id2";
    if ("sub_id3".equals(subGroup)) return ", sub_id3";
    if ("sub_id4".equals(subGroup)) return ", sub_id4";
    return "";
  }
  */

  private String countSql(String sql) {
    sql = sql.replaceFirst("select .* from ", "select count(*) from ");
    sql = sql.substring(0, sql.lastIndexOf(" order by "));
    return "select count(*) from (" + sql + ") c";
  }
}
