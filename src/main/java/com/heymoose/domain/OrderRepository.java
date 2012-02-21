package com.heymoose.domain;

import java.math.BigDecimal;

import com.heymoose.domain.base.Repository;

public interface OrderRepository extends Repository<Order> {
  Iterable<Order> list(int offset, int limit);
  Iterable<Order> list(Ordering ord, boolean asc, int offset, int limit, Long userId);
  Iterable<Order> priceOff(BigDecimal c);
  long count(Long userId);
  
  public enum Ordering
  {
    ID,
    TITLE,
    URL,
    CPA,
    CREATION_TIME,
    DISABLED,
    USER_LAST_NAME
  }
}
