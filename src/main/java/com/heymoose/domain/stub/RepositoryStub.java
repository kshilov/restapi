package com.heymoose.domain.stub;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repository;

import java.util.Map;
import java.util.Set;

public abstract class RepositoryStub<T extends IdEntity> implements Repository<T> {

  protected final Map<Long, T> identityMap = Maps.newHashMap();
  protected long counter = 1;

  @Override
  public T byId(long id) {
    return identityMap.get(id);
  }

  @Override
  public void put(T entity) {
    entity.id = counter++;
    identityMap.put(entity.id, entity);
  }

  @Override
  public Set<T> all() {
    return ImmutableSet.copyOf(identityMap.values());
  }
}
