package com.heymoose.domain.hiber;

import com.google.common.collect.Sets;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.OrderRepository.Ordering;
import com.heymoose.domain.base.Repository.Direction;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class OrderRepositoryHiber extends RepositoryHiber<Order> implements OrderRepository {

  @Inject
  public OrderRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Iterable<Order> list(int offset, int limit) {
    return hiber()
        .createQuery("from Order")
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }
  
  @Override
  public Iterable<Order> list(Ordering ordering, Direction direction, 
      int offset, int limit, int userId)
  {
    String ord = "";
    switch (ordering)
    {
      case ID: ord = "id"; break;
      case CREATION_TIME: ord = "creationTime"; break;
      case CPA: ord = "cpa"; break;
      case USER_NICKNAME: ord = "user.nickname"; break;
      case USER_EMAIL: ord = "user.email"; break;
      case APPROVED: ord = "approved"; break;
      case DELETED: ord = "deleted"; break;
      default: ord = "creationTime"; break;
    }
    
    Criteria criteria = hiber().createCriteria(getEntityClass());
    
    if (userId > 0)
      criteria.add(Restrictions.eq("user.id", userId));
    
    if (direction == Direction.ASC)
      criteria.addOrder(org.hibernate.criterion.Order.asc(ord));
    else
      criteria.addOrder(org.hibernate.criterion.Order.desc(ord));
    
    if (offset > 0 || limit > 0)
    {
      criteria.setFirstResult(offset);
      criteria.setMaxResults(limit);
    }
    
    return criteria.list();
  }
  
  @Override
  public long count() {
    return Long.parseLong(hiber()
        .createQuery("select count(*) from Order")
        .uniqueResult()
        .toString());
  }

  @Override
  public Order byId(long id) {
    return (Order) hiber()
        .createQuery("from Order where deleted = false and id = :id")
        .setParameter("id", id)
        .uniqueResult();
  }

  @Override
  public Set<Order> all() {
    return Sets.newHashSet(hiber()
        .createQuery("from Order where deleted = false")
        .list());
  }

  @Override
  protected Class<Order> getEntityClass() {
    return Order.class;
  }
}
