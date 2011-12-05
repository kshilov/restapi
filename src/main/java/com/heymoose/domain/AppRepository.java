package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface AppRepository extends Repository<App> {
  App anyById(long appId);
  Iterable<App> list(int offset, int limit);
  long count();
}
