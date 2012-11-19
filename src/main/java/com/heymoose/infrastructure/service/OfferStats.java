package com.heymoose.infrastructure.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
import com.heymoose.infrastructure.util.db.TemplateQuery;
import com.heymoose.resource.xml.XmlOverallOfferStats;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.heymoose.infrastructure.util.HibernateUtil.addEqOrIsNull;

@Singleton
public class OfferStats {

  public static enum Ordering {
    DESCR, SHOWS_COUNT, CLICKS_COUNT, LEADS_COUNT, SALES_COUNT,
    CONFIRMED_REVENUE, NOT_CONFIRMED_REVENUE, CANCELED_REVENUE,
    CTR, CR, ECPC, ECPM
  }

  public static final class CommonParams extends DataFilter<Ordering> {

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

  private final Repo repo;

  @Inject
  public OfferStats(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public Pair<QueryResult, Long> allOfferStats(boolean granted,
                                               CommonParams commonParams) {
    return offerStats(commonParams)
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("granted", granted)
        .executeAndCount(commonParams.offset(), commonParams.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> affOfferStats(Long affiliateId,
                                               CommonParams common) {
    return offerStats(common)
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("filterByAffiliate", true)
        .addQueryParam("aff_id", affiliateId)
        .executeAndCount(common.offset(), common.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> advOfferStats(Long advertiserId,
                                               CommonParams common) {
    return offerStats(common)
        .addTemplateParam("groupByOffer", true)
        .addTemplateParam("addFee", true)
        .addTemplateParam("filterByAdvertiser", true)
        .addQueryParam("adv_id", advertiserId)
        .executeAndCount(common.offset(), common.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> affStats(CommonParams common) {

    return offerStats(common)
        .addTemplateParam("groupByAffiliate", true)
        .executeAndCount(common.offset(), common.limit());
  }


  @Transactional
  public Pair<QueryResult, Long> advStats(CommonParams common) {
    return offerStats(common)
        .addTemplateParam("groupByAdvertiser", true)
        .addTemplateParam("addFee", true)
        .executeAndCount(common.offset(), common.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> affStatsByOffer(
      long offerId, boolean forAdvertiser, CommonParams common) {
    TemplateQuery query = offerStats(common)
        .addTemplateParam("filterByOffer", true)
        .addQueryParam("offer_id", offerId);
    if (forAdvertiser) {
      query.addTemplateParam("groupByAffiliateId", true);
      query.addTemplateParam("addFee", true);
    } else {
      query.addTemplateParam("groupByAffiliate", true);
    }
    return query.executeAndCount(common.offset(), common.limit());
  }


  @Transactional
  public Pair<QueryResult, Long> sourceIdStats(Long affId,
                                               Long offerId,
                                               CommonParams common) {
    return offerStats(common)
        .addTemplateParam("groupBySourceId", true)
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addTemplateParamIfNotNull(offerId, "offer_id", offerId)
        .executeAndCount(common.offset(), common.limit());
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
      Set<String> grouping, CommonParams common) {

    // empty response if no grouping given
    if (grouping.size() == 0) return Pair.of(QueryResult.empty(), 0L);

    return offerStats(common)
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", affId)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addQueryParamIfNotNull(offerId, "offer_id", offerId)
        .addTemplateParam("groupBySub", grouping)
        .addTemplateParam("filterBySub", filter.keySet())
        .addQueryParamsFromMap(filter)
        .executeAndCount(common.offset(), common.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> cashbackStats(Long affId, CommonParams common) {
    return offerStats(common)
        .addTemplateParam("filterByAffiliate", true)
        .addQueryParam("aff_id", affId)
        .addTemplateParam("groupByCashback", true)
        .executeAndCount(common.offset(), common.limit());
  }


  @Transactional
  public Pair<QueryResult, Long> refererStats(
      Long affId, Long offerId, CommonParams common) {
    return offerStats(common)
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addQueryParamIfNotNull(offerId, "offer_id", offerId)
        .addTemplateParam("groupByReferer", true)
        .executeAndCount(common.offset(), common.limit());
  }

  @Transactional
  public Pair<QueryResult, Long> keywordsStats(
      Long affId, Long offerId, CommonParams common) {
    return offerStats(common)
        .addTemplateParamIfNotNull(affId, "filterByAffiliate", true)
        .addQueryParamIfNotNull(affId, "aff_id",affId)
        .addTemplateParamIfNotNull(offerId, "filterByOffer", true)
        .addQueryParamIfNotNull(offerId, "offer_id", offerId)
        .addTemplateParam("groupByKeywords", true)
        .executeAndCount(common.offset(), common.limit());
  }

  @SuppressWarnings("unchecked")
  public Pair<QueryResult, Long> subofferStatForOffer(Long affId,
                                                      Long offerId,
                                                      boolean forAdvertiser,
                                                      CommonParams common) {
    Preconditions.checkNotNull(offerId, "Offer id should not be null.");
    return subOfferStats(common)
        .addTemplateParam("addFee", forAdvertiser)
        .addTemplateParam("filterByParentId", true)
        .addQueryParam("parent_id", offerId)
        .addTemplateParamIfNotNull(affId, "filterByAffId", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .executeAndCount(common.offset(), common.limit());
  }

  public Pair<QueryResult, Long> subofferStatForAffiliate(Long affId,
                                                          CommonParams common) {
    Preconditions.checkNotNull(affId, "Affiliate id should not be null");
    return subOfferStats(common)
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("aff_id", affId)
        .executeAndCount(common.offset(), common.limit());
  }

  public Pair<QueryResult, Long> subofferStatForAdvertiser(Long advId,
                                                           CommonParams common) {
    Preconditions.checkNotNull(advId, "Advertiser id should not be null");
    return subOfferStats(common)
        .addTemplateParam("filterByAdvId", true)
        .addQueryParam("adv_id", advId)
        .executeAndCount(common.offset(), common.limit());
  }

  public Pair<QueryResult, Long> subofferStatForSubIds(Long affId, Long offerId,
                                                       ImmutableMap<String, String> subIdFilter,
                                                       CommonParams common) {
    TemplateQuery query = subOfferStats(common)
        .addTemplateParam("filterBySubId", subIdFilter.keySet())
        .addQueryParamsFromMap(subIdFilter)
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByParentId", true)
        .addQueryParamIfNotNull(offerId, "parent_id", offerId);
    if (subIdFilter.isEmpty()) query.addTemplateParam("emptySubIdsOnly", true);
    return query.executeAndCount(common.offset(), common.limit());
  }


  public Pair<QueryResult, Long> subofferStatForSourceId(Long affId,
                                                         Long offerId,
                                                         String sourceId,
                                                         CommonParams common) {
    return subOfferStats(common)
        .addTemplateParam("filterBySourceId", true)
        .addQueryParam("source_id", sourceId)
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("aff_id", affId)
        .addTemplateParamIfNotNull(offerId, "filterByParentId", true)
        .addQueryParamIfNotNull(offerId, "parent_id", offerId)
        .executeAndCount(common.offset(), common.limit());
  }

  public Pair<QueryResult, Long> subofferStatForReferer(Long affId,
                                                        Long offerId,
                                                        String referer,
                                                        CommonParams common) {
    return subOfferStats(common)
        .addTemplateParam("filterByReferer", true)
        .addQueryParam("referer", referer)
        .addTemplateParamIfNotNull(offerId, "filterByParentId", offerId)
        .addQueryParamIfNotNull(offerId, "parent_id", offerId)
        .addTemplateParamIfNotNull(affId, "filterByAffId", affId)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .executeAndCount(common.offset(), common.limit());
  }

  public Pair<QueryResult, Long> subofferStatForKeywords(Long affId,
                                                         Long offerId,
                                                         String keywords,
                                                         CommonParams common) {
    return subOfferStats(common)
        .addTemplateParam("filterByKeywords", true)
        .addQueryParam("keywords", keywords)
        .addTemplateParamIfNotNull(offerId, "filterByParentId", true)
        .addQueryParamIfNotNull(offerId, "parent_id", offerId)
        .addTemplateParamIfNotNull(affId, "filterByAffId", true)
        .addQueryParamIfNotNull(affId, "aff_id", affId)
        .executeAndCount(common.offset(), common.limit());
  }


  public Pair<QueryResult, Long> subofferStatForCashback(Long affId,
                                                         String cashback,
                                                         CommonParams common) {
    return subOfferStats(common)
        .addTemplateParam("filterByAffId", true)
        .addQueryParam("aff_id", affId)
        .addTemplateParam("filterByCashback", true)
        .addQueryParam("cashback", cashback)
        .executeAndCount(common.offset(), common.limit());
  }


  public OfferStat findStat(OfferStat stat) {
    return findStat(stat.bannerId(), stat.offerId(),
        stat.affiliateId(), stat.sourceId(), stat.subs(), stat.referer(),
        stat.keywords(), stat.cashbackTargetId(), stat.cashbackReferrer());
  }

  public OfferStat findStat(
      @Nullable Long bannerId, long offerId, long affId, String sourceId, Subs subs,
      @Nullable String referer, @Nullable String keywords,
      String cashbackTargetId, String cashbackReferer) {

    DetachedCriteria criteria = DetachedCriteria.forClass(OfferStat.class)
        .add(Restrictions.eq("offer.id", offerId))
        .add(Restrictions.eq("affiliate.id", affId));
    addEqOrIsNull(criteria, "bannerId", bannerId);
    addEqOrIsNull(criteria, "sourceId", sourceId);
    addEqOrIsNull(criteria, "subId", subs.subId());
    addEqOrIsNull(criteria, "subId1", subs.subId1());
    addEqOrIsNull(criteria, "subId2", subs.subId2());
    addEqOrIsNull(criteria, "subId3", subs.subId3());
    addEqOrIsNull(criteria, "subId4", subs.subId4());
    addEqOrIsNull(criteria, "referer", referer);
    addEqOrIsNull(criteria, "keywords", keywords);
    addEqOrIsNull(criteria, "cashbackTargetId", cashbackTargetId);
    addEqOrIsNull(criteria, "cashbackReferrer", cashbackReferer);
    criteria.add(Restrictions.ge("creationTime", DateTime.now().minusHours(1)));
    return repo.byCriteria(criteria);
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

  private TemplateQuery offerStats(CommonParams params) {
    return commonQuery("offer_stats", params);
  }

  private TemplateQuery subOfferStats(CommonParams params) {
    return commonQuery("suboffer_stats", params);
  }

  private TemplateQuery commonQuery(String name, CommonParams params) {
    return SqlLoader.templateQuery(name, repo.session())
        .addTemplateParam("ordering", params.ordering())
        .addTemplateParam("direction", params.direction())
        .addQueryParam("from", params.from())
        .addQueryParam("to", params.to());
  }
}
