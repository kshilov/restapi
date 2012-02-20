package com.heymoose.domain.hiber;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Sets;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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
  
  protected static Order order(String propertyName, boolean ascending) {
    return ascending ? Order.asc(propertyName) : Order.desc(propertyName);
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
  public void remove(T entity) {
    hiber().delete(entity);
  }

  @Override
  public Set<T> all() {
    return Sets.newHashSet(hiber().createCriteria(getEntityClass()).list());
  }

  @Override
  public Map<Long, T> byIds(Iterable<Long> ids) {
    Map<Long, T> found = newHashMap();
    if (isEmpty(ids))
      return found;
    List<T> list = hiber()
        .createCriteria(getEntityClass())
        .add(Restrictions.in("id", newArrayList(ids)))
        .list();
    for (T entity : list)
      found.put(entity.id(), entity);
    return found;
  }
}
