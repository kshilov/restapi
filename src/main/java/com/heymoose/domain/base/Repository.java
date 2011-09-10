package com.heymoose.domain.base;

import java.util.Set;

public interface Repository<T extends IdEntity> {
  T byId(long id);
  void put(T entity);
  Set<T> all();
}
