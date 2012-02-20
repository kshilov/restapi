package com.heymoose.domain.hiber;

import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@Singleton
public class OrderRepositoryHiber extends RepositoryHiber<Order> implements OrderRepository {
  
  @Inject
  public OrderRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Iterable<Order> list(int offset, int limit) {
    return hiber()
        .createQuery("from Order order by creationTime desc")
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }
  
  @Override
  public Iterable<Order> list(Ordering ord, boolean asc, int offset, int limit, Long userId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (userId != null)
      criteria.add(Restrictions.eq("user.id", userId));
    
    setOrdering(criteria, ord, asc);
    return criteria
      .setFetchMode("offer.banners", FetchMode.SELECT)
      .setFirstResult(offset)
      .setMaxResults(limit)
      .list();
  }
  
  @Override
  public long count(Long userId) {
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (userId != null)
      criteria.add(Restrictions.eq("user.id", userId));
    
    return Long.parseLong(criteria
        .setProjection(Projections.rowCount())
        .uniqueResult().toString());
  }

  @Override
  protected Class<Order> getEntityClass() {
    return Order.class;
  }
  
  private static void setOrdering(Criteria criteria, Ordering ord, boolean asc) {
    switch (ord) {
    case ID: criteria.addOrder(order("id", asc)); break;
    case TITLE: criteria.createAlias("offer", "offer").addOrder(order("offer.title", asc)); break;
    case URL: criteria.createAlias("offer", "offer").addOrder(order("offer.url", asc)); break;
    case CPA: criteria.addOrder(order("cpa", asc)); break;
    case DISABLED: criteria.addOrder(order("disabled", asc)); break;
    case CREATION_TIME: criteria.addOrder(order("creationTime", asc)); break;
    case USER_LAST_NAME: criteria.createAlias("user", "user").addOrder(order("user.lastName", asc)); break;
    }
    
    if (ord != Ordering.ID)
      criteria.addOrder(order("id", asc));
  }
}
