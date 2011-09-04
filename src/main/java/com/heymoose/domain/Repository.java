package com.heymoose.domain;

public interface Repository<T extends IdEntity> {
  T get(long id);
  void put(T entity);
}
