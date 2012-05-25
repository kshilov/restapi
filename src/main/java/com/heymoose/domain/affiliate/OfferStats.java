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

  public int queryInt(Query query) {
    return extractLong(query.uniqueResult()).intValue();
  }

  private List<OverallOfferStats> toStats(List<Object[]> dbResult) {
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
      result.add(new OverallOfferStats(offerId, name, shows, clicks, leads, sales,
          confirmedRevenue, notConfirmedRevenue, canceledRevenue, ctr, cr, ecpc, ecpm));
    }
    return result;
  }

  private List<OverallOfferStats> queryStats(String query, DateTime from, DateTime to, int offset, int limit) {
    return toStats(repo.session()
        .createSQLQuery(query)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list()
    );
  }

  @Transactional
  public List<OverallOfferStats> offerStatsAll(DateTime from, DateTime to, int offset, int limit) {
    String query = "select offer.id a1, offer.name a2, sum(show_count) a3, sum(coalesce(click_count, 0)) a4, " +
        "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
        "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9 " +
        "from offer " +
        "left join offer_stat on offer_stat.creation_time between :from and :to and " +
        "offer.id = offer_stat.master " +
        "where offer.parent_id is null group by offer.id, offer.name order by a4 desc " +
        "offset :offset limit :limit";
    return queryStats(query, from, to, offset, limit);
  }

  @Transactional
  public int offerCountAll(DateTime from, DateTime to) {
    String query = "select count(*) from (select 1 from offer " +
    		"left join offer_stat on offer_stat.creation_time between :from and :to " +
    		"and offer.id = offer_stat.master where parent_id is null group by offer.id) as _t" ;
    return queryInt(repo.session()
        .createSQLQuery(query)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
    );
  }

  @Transactional
  public List<OverallOfferStats> grantedOfferStatsAll(DateTime from, DateTime to, int offset, int limit) {
    String query = "select o.id a1, o.name a2, sum(show_count) a3, sum(coalesce(click_count, 0)) a4, sum(leads_count) a5, " +
        "sum(sales_count) a6, sum(confirmed_revenue) a7, sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9 " +
        "from offer_grant g join offer o on g.offer_id = o.id left join offer_stat " +
        "on offer_stat.creation_time between :from and :to and g.offer_id = master and g.aff_id = offer_stat.aff_id " +
        "where g.state = 'APPROVED' group by o.id, o.name order by a4 desc offset :offset limit :limit";
    return queryStats(query, from, to, offset, limit);
  }

  @Transactional
  public int grantedOfferCountAll(DateTime from, DateTime to) {
    String query = "select count(*) from (select g.offer_id from offer_grant g left join offer_stat " +
        "on offer_stat.creation_time between :from and :to and g.offer_id = master and g.aff_id = offer_stat.aff_id " +
        "where g.state = 'APPROVED' group by g.offer_id) _" ;
    return queryInt(repo.session()
        .createSQLQuery(query)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
    );
  }

  @Transactional
  public List<OverallOfferStats> offerStatsByAff(long affId, DateTime from, DateTime to, int offset, int limit) {
    String query = "select g.offer_id a1, o.name a2, sum(show_count) a3, sum(click_count) a4, " +
        "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
        "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9 " +
        "from offer_grant g " +
        "join offer o on g.offer_id = o.id " +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and g.offer_id = master and g.aff_id = offer_stat.aff_id " +
        "where g.state = 'APPROVED' and g.aff_id = :affId " +
        "group by g.offer_id, o.name, g.creation_time order by g.creation_time desc " +
        "offset :offset limit :limit";
    List<Object[]> dbResult = repo.session()
        .createSQLQuery(query)
        .setParameter("affId", affId)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list();
    return toStats(dbResult);
  }

  @Transactional
  public int offerCountByAff(long affId, DateTime from, DateTime to) {
    Query query = repo.session()
        .createSQLQuery("select count(*) from (select 1 from offer_grant g " +
        		"left join offer_stat offer_stat.creation_time between :from and :to " +
        		"and g.offer_id = master and g.aff_id = offer_stat.aff_id where g.state = 'APPROVED' and g.aff_id = :affId group by g.offer_id) as _t")
        .setParameter("affId", affId)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate());
    return queryInt(query);
  }

  @Transactional
  public List<OverallOfferStats> offerStatsByAdv(long advId, DateTime from, DateTime to, int offset, int limit) {
    String query = "select offer.id a1, offer.name a2, sum(show_count) a3, sum(click_count) a4, " +
        "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, " +
        "sum(not_confirmed_revenue) a8, sum(canceled_revenue) a9 " +
        "from offer " +
        "left join offer_stat on offer_stat.creation_time between :from and :to " +
        "and offer.id = offer_stat.master " +
        "where offer.user_id = :advId group by offer.id, offer.name order by offer.id desc " +
        "offset :offset limit :limit";
    List<Object[]> dbResult = repo.session()
        .createSQLQuery(query)
        .setParameter("advId", advId)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list();
    return toStats(dbResult);
  }

  @Transactional
  public int offerCountByAdv(long advId, DateTime from, DateTime to) {
    Query query = repo.session()
        .createSQLQuery("select count(*) from (select 1 from offer " +
        		"left join offer_stat where offer_stat.creation_time between :from and :to " +
        		"and offer.id = offer_stat.master where offer.user_id = :advId group by offer.id) as _t")
        .setParameter("advId", advId)
        .setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate());
    return queryInt(query);
  }

  @Transactional
  public List<OverallOfferStats> affStats(DateTime from, DateTime to, int offset, int limit) {
    String query = "select g.aff_id a2, p.first_name || ' ' || p.last_name, sum(show_count) a3, sum(coalesce(click_count, 0)) a4, " +
        "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, sum(not_confirmed_revenue) a8, " +
        "sum(canceled_revenue) a9 " +
        "from offer_grant g join offer o on g.offer_id = o.id " +
        "left join offer_stat on offer_stat.creation_time between :from and :to and g.offer_id = master and g.aff_id = offer_stat.aff_id " +
        "left join user_profile p on g.aff_id = p.id where g.state = 'APPROVED' " +
        "group by g.aff_id, p.first_name, p.last_name order by a4 desc offset :offset limit :limit";
    List<Object[]> dbResult = repo.session()
        .createSQLQuery(query)
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list();
    return toStats(dbResult);
  }

  @Transactional
  public int affCount(DateTime from, DateTime to) {
    Query query = repo.session()
        .createSQLQuery("select count(*) from (select g.aff_id a1 from offer_grant g join offer o on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to " +
            "and g.offer_id = master and g.aff_id = offer_stat.aff_id where g.state = 'APPROVED' group by g.aff_id) _")
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate());
    return queryInt(query);
  }

  @Transactional
  public List<OverallOfferStats> affStatsByOffer(long offerId, DateTime from, DateTime to, int offset, int limit) {
    String query = "select g.aff_id a2, p.first_name || ' ' || p.last_name, sum(show_count) a3, sum(coalesce(click_count, 0)) a4, " +
        "sum(leads_count) a5, sum(sales_count) a6, sum(confirmed_revenue) a7, sum(not_confirmed_revenue) a8, " +
        "sum(canceled_revenue) a9 " +
        "from offer_grant g join offer o on g.offer_id = o.id " +
        "left join offer_stat on offer_stat.creation_time between :from and :to and g.offer_id = master and g.aff_id = offer_stat.aff_id " +
        "left join user_profile p on g.aff_id = p.id where g.state = 'APPROVED' and g.offer_id = :offer " +
        "group by g.aff_id, p.first_name, p.last_name order by a4 desc offset :offset limit :limit";
    List<Object[]> dbResult = repo.session()
        .createSQLQuery(query)
        .setParameter("offer", offerId)
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate())
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list();
    return toStats(dbResult);
  }

  @Transactional
  public int affCountByOffer(long offerId, DateTime from, DateTime to) {
    Query query = repo.session()
        .createSQLQuery("select count(*) from (select g.aff_id a1 from offer_grant g join offer o on g.offer_id = o.id " +
            "left join offer_stat on offer_stat.creation_time between :from and :to " +
            "and g.offer_id = master and g.aff_id = offer_stat.aff_id where g.state = 'APPROVED' and g.offer_id = :offer group by g.aff_id) _")
        .setParameter("offer", offerId)
        .setParameter("from", from.toDate())
        .setParameter("to", to.toDate());
    return queryInt(query);
  }

  private static Long extractLong(Object val) {
    if (val == null)
      return 0L;
    if (val instanceof BigInteger)
      return ((BigInteger) val).longValue();
    if (val instanceof BigDecimal)
      return ((BigDecimal) val).longValue();
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
}
