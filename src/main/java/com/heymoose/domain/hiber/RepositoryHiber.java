package com.heymoose.domain.hiber;

import com.google.common.collect.Sets;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repository;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

public abstract class RepositoryHiber<T extends IdEntity> implements Repository<T> {

  private final Provider<Session> sessionProvider;

  @Inject
  public RepositoryHiber(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  protected abstract Class<T> getEntityClass();

  protected Session hiber() {
    return sessionProvider.get();
  }

  @Override
  public T byId(long id) {
    return (T) hiber().get(getEntityClass(), id);
  }

  @Override
  public void put(T entity) {
    hiber().saveOrUpdate(entity);
  }

  @Override
  public Set<T> all() {
    return Sets.newHashSet(hiber().createCriteria(getEntityClass()).list());
  }
}
