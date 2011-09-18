package com.heymoose.domain.stub;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.util.Paging;
import com.sun.org.apache.xpath.internal.operations.Or;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Singleton
public class OrderRepositoryStub extends RepositoryStub<Order> implements OrderRepository {
  @Override
  public Order byId(long id) {
    Order order = super.byId(id);
    if (order.deleted)
      return null;
    return order;
  }

  @Override
  public Set<Order> all() {
    Set<Order> orders = Sets.newHashSet();
    for (Order order : super.all())
      orders.add(order);
    return Collections.unmodifiableSet(orders);
  }

  @Override
  public Iterable<Order> list(int offset, int limit) {
    List<Order> all = Lists.newArrayList(identityMap.values());
    List<Order> page = Paging.page(all, offset, limit);
    return Collections.unmodifiableList(page);
  }
}
