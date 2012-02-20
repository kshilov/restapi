package com.heymoose.domain.base;

import java.util.Map;
import java.util.Set;

public interface Repository<T extends IdEntity> {
  T byId(long id);
  Map<Long, T> byIds(Iterable<Long> ids);
  void put(T entity);
  Set<T> all();
  void remove(T entity);
}
