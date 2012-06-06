package com.heymoose.domain.affiliate;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
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

    private List<OverallOfferStats> toStats(List<Object[]> dbResult, String subGroup) {
        List<OverallOfferStats> result = newArrayList();
        for (Object[] record : dbResult) {
            long offerId = extractLong(record[0]);
            String name = (String) record[1];
            long shows = extractLong(record[2]);
            long clicks = extractLong(record[3]);
            long leads = extractLong(record[4]);
            long sales = extractLong(record[5]);
            double confirmedRevenue = extractDouble(record[6]);
            double notConfirmedRevenue = extractDouble(record[7]);
            double canceledRevenue = extractDouble(record[8]);
            Double ctr = (shows == 0)
                ? null
                : clicks * 100.0 / shows;
            Double cr = (clicks == 0)
                ? null
                : (leads + sales) * 100.0 / clicks;
            Double ecpc = (clicks == 0)
                ? null
                : (confirmedRevenue + notConfirmedRevenue) / clicks;
            Double ecpm = (shows == 0)
                ? null
                : (confirmedRevenue + notConfirmedRevenue) * 1000.0 / shows;
            String sourceId = "source_id".equals(subGroup) ? (String) record[9] : null;
            String subId = "sub_id".equals(subGroup) ? (String) record[9] : null;
            String subId1 = "sub_id1".equals(subGroup) ? (String) record[9] : null;
            String subId2 = "sub_id2".equals(subGroup) ? (String) record[9] : null;
            String subId3 = "sub_id3".equals(subGroup) ? (String) record[9] : null;
            String subId4 = "sub_id4".equals(subGroup) ? (String) record[9] : null;

            result.add(new OverallOfferStats(offerId, name, shows, clicks, leads, sales,
                confirmedRevenue, notConfirmedRevenue, canceledRevenue, ctr, cr, ecpc, ecpm,
                sourceId, subId, subId1, subId2, subId3, subId4));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<OverallOfferStats> queryStats(
        String sql, Subs subs, DateTime from, DateTime to, int offset, int limit) {
        Query query = repo.session().createSQLQuery(sql);
        addSubsParametersToQuery(subs, query);

        return toStats(query
            .setTimestamp("from", from.toDate())
            .setTimestamp("to", to.toDate())
            .setParameter("offset", offset)
            .setParameter("limit", limit)
            .list(), subs.subGroup()
        );
    }

    @Transactional
    public List<OverallOfferStats> offerStatsAll(
        Subs subs, DateTime from, DateTime to, int offset, int limit) {
        String sql = "select offer.id a1, offer.name a2, sum(show_count) a3, sum(coalesce(click_count, 0)) a4, " +
            "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
            "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9" + addSubGroupToSqlInSelect(subs.subGroup()) + " " +
            "from offer left join offer_stat on offer_stat.creation_time between :from and :to " +
            "and offer.id = offer_stat.master " + addSubsToSql(subs) +
            "where offer.parent_id is null group by offer.id, offer.name" + addSubGroupToSqlInGroupBy(subs.subGroup()) + " " +
            "order by a4 desc offset :offset limit :limit";

        return queryStats(sql, subs, from, to, offset, limit);
    }

    @Transactional
    public List<OverallOfferStats> grantedOfferStatsAll(
        Subs subs, DateTime from, DateTime to, int offset, int limit) {
        String sql = "select o.id a1, o.name a2, sum(show_count) a3, sum(coalesce(click_count, 0)) a4, " +
            "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, sum(not_confirmed_revenue) a8, " +
            "sum(canceled_revenue) a9" + addSubGroupToSqlInSelect(subs.subGroup()) + " " +
            "from offer_grant g join offer o on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to " +
            "and g.offer_id = master and g.aff_id = offer_stat.aff_id " + addSubsToSql(subs) +
            "where g.state = 'APPROVED' group by o.id, o.name" + addSubGroupToSqlInGroupBy(subs.subGroup()) + " " +
            "order by a4 desc offset :offset limit :limit";

        return queryStats(sql, subs, from, to, offset, limit);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<OverallOfferStats> offerStatsByAff(
        long affId, Subs subs, DateTime from, DateTime to, int offset, int limit) {
        String sql = "select g.offer_id a1, o.name a2, sum(show_count) a3, sum(click_count) a4, " +
            "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
            "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9" + addSubGroupToSqlInSelect(subs.subGroup()) + " " +
            "from offer_grant g join offer o on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to " +
            "and g.offer_id = master and g.aff_id = offer_stat.aff_id " + addSubsToSql(subs) +
            "where g.state = 'APPROVED' and g.aff_id = :affId " +
            "group by g.offer_id, o.name, g.creation_time" + addSubGroupToSqlInGroupBy(subs.subGroup()) + " " +
            "order by g.creation_time desc offset :offset limit :limit";

        Query query = repo.session().createSQLQuery(sql);
        addSubsParametersToQuery(subs, query);

        List<Object[]> dbResult = query
            .setParameter("affId", affId)
            .setTimestamp("from", from.toDate())
            .setTimestamp("to", to.toDate())
            .setParameter("offset", offset)
            .setParameter("limit", limit)
            .list();
        return toStats(dbResult, subs.subGroup());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<OverallOfferStats> offerStatsByAdv(
        long advId, Subs subs, DateTime from, DateTime to, int offset, int limit) {
        String sql = "select offer.id a1, offer.name a2, sum(show_count) a3, sum(click_count) a4, " +
            "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
            "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9" + addSubGroupToSqlInSelect(subs.subGroup()) + " " +
            "from offer left join offer_stat on offer_stat.creation_time between :from and :to " +
            "and offer.id = offer_stat.master " + addSubsToSql(subs) +
            "where offer.user_id = :advId group by offer.id, offer.name" + addSubGroupToSqlInGroupBy(subs.subGroup()) + " " +
            "order by offer.id desc offset :offset limit :limit";

        Query query = repo.session().createSQLQuery(sql);
        addSubsParametersToQuery(subs, query);

        List<Object[]> dbResult = query
            .setParameter("advId", advId)
            .setTimestamp("from", from.toDate())
            .setTimestamp("to", to.toDate())
            .setParameter("offset", offset)
            .setParameter("limit", limit)
            .list();
        return toStats(dbResult, subs.subGroup());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<OverallOfferStats> affStats(
        Subs subs, DateTime from, DateTime to, int offset, int limit) {
        String sql = "select g.aff_id a2, p.first_name || ' ' || p.last_name, " +
            "sum(show_count) a3, sum(coalesce(click_count, 0)) a4, " +
            "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
            "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9" + addSubGroupToSqlInSelect(subs.subGroup()) + " " +
            "from offer_grant g join offer o on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to and g.offer_id = master " +
            "and g.aff_id = offer_stat.aff_id " + addSubsToSql(subs) +
            "left join user_profile p on g.aff_id = p.id where g.state = 'APPROVED' " +
            "group by g.aff_id, p.first_name, p.last_name" + addSubGroupToSqlInGroupBy(subs.subGroup()) + " " +
            "order by a4 desc offset :offset limit :limit";

        Query query = repo.session().createSQLQuery(sql);
        addSubsParametersToQuery(subs, query);
        query
            .setParameter("from", from.toDate())
            .setParameter("to", to.toDate())
            .setParameter("offset", offset)
            .setParameter("limit", limit);

        return toStats(query.list(), subs.subGroup());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<OverallOfferStats> affStatsByOffer(
        long offerId, Subs subs, DateTime from, DateTime to, int offset, int limit) {
        String sql = "select g.aff_id a2, p.first_name || ' ' || p.last_name, sum(show_count) a3, " +
            "sum(coalesce(click_count, 0)) a4, sum(leads_count) a5, sum(sales_count) a6, " +
            "sum(confirmed_revenue) a7, sum(not_confirmed_revenue) a8, " +
            "sum(canceled_revenue) a9" + addSubGroupToSqlInSelect(subs.subGroup()) + " " +
            "from offer_grant g join offer o on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to and g.offer_id = master " +
            "and g.aff_id = offer_stat.aff_id " + addSubsToSql(subs) +
            "left join user_profile p on g.aff_id = p.id where g.state = 'APPROVED' and g.offer_id = :offer " +
            "group by g.aff_id, p.first_name, p.last_name" + addSubGroupToSqlInGroupBy(subs.subGroup()) + " " +
            "order by a4 desc offset :offset limit :limit";

        Query query = repo.session().createSQLQuery(sql);
        addSubsParametersToQuery(subs, query);
        query
            .setParameter("offer", offerId)
            .setParameter("from", from.toDate())
            .setParameter("to", to.toDate())
            .setParameter("offset", offset)
            .setParameter("limit", limit);

        return toStats(query.list(), subs.subGroup());
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

    private void addSubsParametersToQuery(Subs subs, Query dbQuery) {
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

}
