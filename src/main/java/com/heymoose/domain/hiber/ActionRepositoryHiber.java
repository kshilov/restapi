package com.heymoose.domain.hiber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.ResultTransformer;
import org.joda.time.DateTime;

@Singleton
public class ActionRepositoryHiber extends RepositoryHiber<Action> implements ActionRepository {

  @Inject
  public ActionRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Action byPerformerAndOfferAndApp(long performerId, long offerId, long appId) {
    return (Action) hiber()
        .createQuery("from Action a where a.performer.id = :performerId and a.offer.id = :offerId and a.deleted = false")
        .setParameter("performerId", performerId)
        .setParameter("offerId", offerId)
        .uniqueResult();
  }
  
  @Override
  public Iterable<Action> list(DateTime from, DateTime to,
      Long offerId, Long appId, Long performerId) {
    Criteria criteria = hiber()
        .createCriteria(getEntityClass())
        .add(Restrictions.between("creationTime", from, to));
    
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (appId != null)
      criteria.add(Restrictions.eq("app.id", appId));
    if (performerId != null)
      criteria.add(Restrictions.eq("performer.id", performerId));

    criteria.addOrder(Order.desc("creationTime"));
        
    return criteria.list();
  }

  @Override
  public Map<DateTime, Integer> stats(DateTime from, DateTime to, Long offerId, Long appId, Long performerId, String trunc) {
    checkArgument(asList("hour", "month", "year", "day").contains(trunc));
    String sql = "select date_trunc('{trunc}', creation_time), count(*) from action" +
        " where creation_time between :from and :to";

    if (offerId != null)
      sql += " and offer_id = :offer";
    if (appId != null)
      sql += " and app_id = :app";
    if (performerId != null)
      sql += " and performer_id = :performer";

    sql += " group by date_trunc('{trunc}', creation_time)";

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
  public Iterable<Action> list(Ordering ordering, int offset, int limit) {
    return list(ordering, offset, limit, null, null, null);
  }
  
  @Override
  public Iterable<Action> list(Ordering ordering, int offset, int limit,
      Long offerId, Long appId, Long performerId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    if (offerId != null)
      criteria.add(Restrictions.eq("offer.id", offerId));
    if (appId != null)
      criteria.add(Restrictions.eq("app.id", appId));
    if (performerId != null)
      criteria.add(Restrictions.eq("performer.id", performerId));
    
    if (ordering.equals(Ordering.BY_CREATION_TIME_DESC))
      criteria.addOrder(Order.desc("creationTime"));
    else if (ordering.equals(Ordering.BY_CREATION_TIME_ASC));
      criteria.addOrder(Order.asc("creationTime"));
      
    criteria.setFirstResult(offset);
    criteria.setMaxResults(limit);
    return criteria.list();
  }
  
  @Override
  public long count() {
    return count(null, null, null);
  }
  
  @Override
  public long count(Long offerId, Long appId, Long performerId) {
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
  protected Class<Action> getEntityClass() {
    return Action.class;
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
    String sql = "select " + column + ", count(*) from action where " + column + " in :ids";
    if (from != null && to != null)
      sql += " and creation_time between :from and :to";
    else if (from != null)
      sql += " and creation_time >= :from";
    else if (to != null)
      sql += " and creation_time <= :to";
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
