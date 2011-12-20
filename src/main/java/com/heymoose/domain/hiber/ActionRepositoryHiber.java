package com.heymoose.domain.hiber;

import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

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
        
    return criteria.list();
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
}
