package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface OrderRepository extends Repository<Order> {
  Iterable<Order> list(int offset, int limit);
}
