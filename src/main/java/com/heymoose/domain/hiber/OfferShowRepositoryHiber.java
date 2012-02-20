package com.heymoose.domain.hiber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.OfferShow;
import com.heymoose.domain.OfferShowRepository;
import java.math.BigInteger;
import java.sql.Timestamp;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

@Singleton
public class OfferShowRepositoryHiber extends RepositoryHiber<OfferShow> implements OfferShowRepository {

  @Inject
  public OfferShowRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<OfferShow> getEntityClass() {
    return OfferShow.class;
  }
  
  @Override
  public Map<DateTime, Integer> stats(DateTime from, DateTime to,
                                      Long offerId, Long appId, Long performerId, String trunc) {

    checkArgument(asList("hour", "month", "year", "day").contains(trunc));
    String sql = "select date_trunc('{trunc}', show_time), count(*) from offer_show" +
        " where show_time between :from and :to";

    if (offerId != null)
      sql += " and offer_id = :offer";
    if (appId != null)
      sql += " and app_id = :app";
    if (performerId != null)
      sql += " and performer_id = :performer";

    sql += " group by date_trunc('{trunc}', show_time)";

    sql = sql.replaceAll("\\{trunc\\}", trunc);

    SQLQuery query = hiber().createSQLQuery(sql);

    query.setTimestamp("from", from.toDate());
    query.setTimestamp("to", to.toDate());

    if (offerId != null)
      query.setLong("offer", offerId);
    if (appId != null)
      query.setLong("app", appId);
    if (performerId != null)
      query.setLong("performer", performerId);

    Map<DateTime, Integer> stats = newHashMap();
    for (Object[] data : (List<Object[]>) query.list()) {
      long time = ((Timestamp) data[0]).getTime();
      int count = ((BigInteger) data[1]).intValue();
      stats.put(new DateTime(time), count);
    }

    return stats;
  }

  @Override
  public Long count(Long offerId, Long appId, Long performerId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (appId != null)
      criteria.add(Restrictions.eq("app.id", appId));
    if (performerId != null)
      criteria.add(Restrictions.eq("performer.id", performerId));
    
    criteria.setProjection(Projections.rowCount());
    return (Long)criteria.uniqueResult();
  }

  @Override
  public Map<Long, Integer> countByApps(List<Long> appIds, DateTime from, DateTime to) {
    return countBy("app_id", appIds, from, to);
  }

  @Override
  public Map<Long, Integer> countByOffers(List<Long> offerIds, DateTime from, DateTime to) {
    return countBy("offer_id", offerIds, from, to);
  }
  
  private Map<Long, Integer> countBy(String column, List<Long> ids, DateTime from, DateTime to) {
    String sql = "select " + column + ", count(*) from offer_show where " + column + " in :ids";
    if (from != null && to != null)
      sql += " and show_time between :from and :to";
    else if (from != null)
      sql += " and show_time >= :from";
    else if (to != null)
      sql += " and show_time <= :to";
    sql += " group by " + column;
    
    SQLQuery query = hiber().createSQLQuery(sql);
    query.setParameterList("ids", ids);
    if (from != null)
      query.setTimestamp("from", from.toDate());
    if (to != null)
      query.setTimestamp("to", to.toDate());
    
    Map<Long, Integer> counts = newHashMap();
    for (Object[] data : (List<Object[]>) query.list()) {
      long id = ((BigInteger)data[0]).longValue();
      int count = ((BigInteger)data[1]).intValue();
      counts.put(id, count);
    }
    
    return counts;
  }
}
