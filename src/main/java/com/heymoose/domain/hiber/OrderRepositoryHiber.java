package com.heymoose.domain.hiber;

import com.google.common.collect.Sets;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import org.hibernate.Session;

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
