package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface OrderRepository extends Repository<Order> {
  Iterable<Order> list(int offset, int limit);
  Iterable<Order> list(Ordering ordering, Direction direction, 
      int offset, int limit, int userId);
  long count();
  
  public enum Ordering
  {
    ID,
    CREATION_TIME,
    CPA,
    USER_NICKNAME,
    USER_EMAIL,
    APPROVED,
    DELETED
  }
}
