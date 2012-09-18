package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
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
    DESCR("name"), SHOWS_COUNT("shows"), CLICKS_COUNT("clicks"),
    LEADS_COUNT("leads"), SALES_COUNT("sales"),
    CONFIRMED_REVENUE("confirmed-revenue"),
    NOT_CONFIRMED_REVENUE("not-confirmed-revenue"),
    CANCELED_REVENUE("canceled-revenue"),
    CTR("ctr"), CR("cr"), ECPC("ecpc"), ECPM("ecpm");

    public final String COLUMN;

    Ordering(String col) {
      this.COLUMN = col;
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

  private static final String STATS_QUERY = "offer_stats";
  private static final String SUBOFFER_STATS_QUERY = "suboffer_stats";


  private static Pair<QueryResult, Long> exec(SqlLoader.TemplateQuery query,
                                              DataFilter<Ordering> params) {
    return query.addQueryParam("from", params.from())
        .addQueryParam("to", params.to())
        .addTemplateParam("ordering", params.ordering().COLUMN)
        .addTemplateParam("direction", params.direction())
        .executeAndCount(params.offset(), params.limit());

  }

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
  public Pair<QueryResult, Long> allOfferStats(boolean granted,
                                               DataFilter<Ordering> common) {
    return exec(SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("granted", granted), common);

  }

  @Transactional
  public Pair<QueryResult, Long> affOfferStats(Long affiliateId,
                                                           DataFilter<Ordering> common) {
    return exec(SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("filterByAffiliate", true)
        .addQueryParam("aff_id", affiliateId), common);
  }

  @Transactional
  public Pair<QueryResult, Long> advOfferStats(Long advertiserId,
                                               DataFilter<Ordering> common) {
    return exec(SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("addFee", true)
        .addTemplateParam("filterByAdvertiser", true)
        .addQueryParam("adv_id", advertiserId), common);
  }

  @Transactional
  public Pair<QueryResult, Long> affStats(DataFilter<Ordering> common) {
    return exec(SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByAffiliate", true), common);
  }


  @Transactional
  public Pair<QueryResult, Long> advStats(DataFilter<Ordering> common) {
    return exec(SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByAdvertiser", true)
        .addTemplateParam("addFee", true), common);
  }

  @Transactional
  public Pair<QueryResult, Long> affStatsByOffer(
      long offerId, boolean forAdvertiser, DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("filterByOffer", true)
        .addQueryParam("offer_id", offerId);
    if (forAdvertiser) {
      query.addTemplateParam("groupByAffiliate", true);
      query.addTemplateParam("addFee", true);
    } else {
      query.addTemplateParam("groupByAffiliate", true);
    }
    return exec(query, common);
  }


  @Transactional
  public Pair<QueryResult, Long> sourceIdStats(Long affId,
                                               Long offerId,
                                               DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupBySourceId", true);
    if (affId != null) {
      query.addTemplateParam("filterByAffiliate", true)
          .addQueryParam("aff_id", affId);
    }
    if (offerId != null) {
      query.addTemplateParam("filterByOffer", true)
          .addQueryParam("offer_id", offerId);
    }
    return exec(query, common);

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
  public Pair<QueryResult, Long> subIdStats(
      Long affId, Long offerId, Map<String, String> filter,
      Set<String> grouping, DataFilter<Ordering> common) {

    // empty response if no grouping given
    if (grouping.size() == 0)
      return Pair.of(QueryResult.empty(), 0L);

    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(STATS_QUERY, repo.session());

    if (affId != null) {
      query.addTemplateParam("filterByAffiliate", true);
      query.addQueryParam("aff_id", affId);
    }
    if (offerId != null) {
      query.addTemplateParam("filterByOffer", true);
      query.addQueryParam("offer_id", offerId);
    }
    query.addTemplateParam("groupBySub", grouping);
    query.addTemplateParam("filterBySub", filter.keySet());
    query.addAllQueryParams(filter);
    return exec(query, common);
  }

  @Transactional
  public Pair<QueryResult, Long> refererStats(
      Long affId, Long offerId, DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByReferer", true);
    if (affId != null) {
      query.addTemplateParam("filterByAffiliate", true);
      query.addQueryParam("aff_id", affId);
    }
    if (offerId != null) {
      query.addTemplateParam("filterByOffer", true);
      query.addQueryParam("offer_id", offerId);
    }
    return exec(query, common);
  }

  @Transactional
  public Pair<QueryResult, Long> keywordsStats(
      Long affId, Long offerId, DataFilter<Ordering> common) {

    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("groupByKeywords", true);
    if (affId != null) {
      query.addTemplateParam("filterByAffiliate", true);
      query.addQueryParam("aff_id", affId);
    }
    if (offerId != null) {
      query.addTemplateParam("filterByOffer", true);
      query.addQueryParam("offer_id", offerId);
    }
    return exec(query, common);
  }

  @SuppressWarnings("unchecked")
  public Pair<QueryResult, Long> subofferStatForOffer(Long affId,
                                                      Long offerId,
                                                      boolean forAdvertiser,
                                                      DataFilter<Ordering> common) {
    Preconditions.checkNotNull(offerId, "Offer id should not be null.");
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(STATS_QUERY, repo.session())
        .addTemplateParam("addFee", forAdvertiser)
        .addTemplateParam("filterByParentId", true)
        .addQueryParam("parent_id", offerId);
    if (affId != null) {
      query.addTemplateParam("filterByAffId", true);
      query.addQueryParam("aff_id", affId);
    }
    return exec(query, common);
  }

  public Pair<QueryResult, Long> subofferStatForAffiliate(Long affId,
                                                          DataFilter<Ordering> common) {
    Preconditions.checkNotNull(affId, "Affiliate id should not be null");
    return exec(SqlLoader.templateQuery(SUBOFFER_STATS_QUERY, repo.session())
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("aff_id", affId), common);
  }

  public Pair<QueryResult, Long> subofferStatForAdvertiser(Long advId,
                                                           DataFilter<Ordering> common) {
    Preconditions.checkNotNull(advId, "Advertiser id should not be null");
    return exec(SqlLoader.templateQuery(SUBOFFER_STATS_QUERY, repo.session())
        .addTemplateParam("filterByAdvId", true)
        .addQueryParam("adv_id", advId), common);
  }

  public Pair<QueryResult, Long> subofferStatForSubIds(Long affId, Long offerId,
                                                       ImmutableMap<String, String> subIdFilter,
                                                       DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(SUBOFFER_STATS_QUERY, repo.session())
        .addTemplateParam("filterBySubId", subIdFilter.keySet())
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("aff_id", affId)
        .addAllQueryParams(subIdFilter);
    if (offerId != null) {
      query.addTemplateParam("filterByParentId", true);
      query.addQueryParam("parent_id", offerId);
    }
    if (subIdFilter.isEmpty()) {
      query.addTemplateParam("emptySubIdsOnly", true);
    }
    return exec(query, common);
  }


  public Pair<QueryResult, Long> subofferStatForSourceId(Long affId,
                                                         Long offerId,
                                                         String sourceId,
                                                         DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(SUBOFFER_STATS_QUERY, repo.session())
        .addTemplateParam("filterBySourceId", true)
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("sourceId", sourceId)
        .addQueryParam("aff_id", affId);
    if (offerId != null) {
      query.addQueryParam("parent_id", offerId);
      query.addTemplateParam("filterByParentId", true);
    }
    return exec(query, common);
  }

  public Pair<QueryResult, Long> subofferStatForReferer(Long affId,
                                                        Long offerId,
                                                        String referer,
                                                        DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(SUBOFFER_STATS_QUERY, repo.session())
        .addTemplateParam("filterByReferer", true)
        .addQueryParam("referer", referer);
    if (offerId != null) {
      query.addQueryParam("parent_id", offerId);
      query.addTemplateParam("filterByParentId", true);
    }
    if (affId != null) {
      query.addTemplateParam("filterByAffId", true);
      query.addQueryParam("aff_id", affId);
    }
    return exec(query, common);
  }

  public Pair<QueryResult, Long> subofferStatForKeywords(Long affId,
                                                         Long offerId,
                                                         String keywords,
                                                         DataFilter<Ordering> common) {
    SqlLoader.TemplateQuery query =
        SqlLoader.templateQuery(SUBOFFER_STATS_QUERY, repo.session())
            .addTemplateParam("filterByKeywords", true)
            .addQueryParam("keywords", keywords);
    if (offerId != null) {
      query.addQueryParam("parent_id", offerId);
      query.addTemplateParam("filterByParentId", true);
    }
    if (affId != null) {
      query.addTemplateParam("filterByAffId", true);
      query.addQueryParam("aff_id", affId);
    }
    return exec(query, common);
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

}
