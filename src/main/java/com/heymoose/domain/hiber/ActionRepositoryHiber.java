package com.heymoose.domain.hiber;

import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

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
  public Action byPerformerAndOffer(long performerId, long offerId) {
    return (Action) hiber()
        .createQuery("from Action a where a.performer.id = :performerId and a.offer.id = :offerId and a.deleted = false")
        .setParameter("performerId", performerId)
        .setParameter("offerId", offerId)
        .uniqueResult();
  }

  @Override
  public Iterable<Action> list(Ordering ordering, int offset, int limit) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
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
    return Long.parseLong(hiber()
        .createQuery("select count(*) from Action")
        .uniqueResult()
        .toString());
  }

  @Override
  protected Class<Action> getEntityClass() {
    return Action.class;
  }
}
