package com.heymoose.domain.stub;

import com.google.common.collect.Maps;
import com.heymoose.domain.IdEntity;
import com.heymoose.domain.Repository;

import java.util.Map;

public abstract class RepositoryStub<T extends IdEntity> implements Repository<T> {

  protected final Map<Long, T> identityMap = Maps.newHashMap();
  protected int counter = 1;

  @Override
  public T get(long id) {
    return identityMap.get(id);
  }

  @Override
  public void put(T entity) {
    entity.setId(counter++);
    identityMap.put(entity.id(), entity);
  }
}
