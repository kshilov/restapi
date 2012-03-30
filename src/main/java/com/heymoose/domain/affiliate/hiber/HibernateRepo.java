package com.heymoose.domain.affiliate.hiber;

import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.domain.base.IdEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

@Singleton
public class HibernateRepo implements Repo {
  private final Provider<Session> sessionProvider;

  @Inject
  public HibernateRepo(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  protected Session hiber() {
    return sessionProvider.get();
  }

  @Override
  public <T extends IdEntity> T get(Class<T> clazz, long id) {
    return (T) hiber().get(clazz, id);
  }

  @Override
  public <T extends IdEntity> void put(T entity) {
    hiber().saveOrUpdate(entity);
  }

  @Override
  public <T extends IdEntity> T byHQL(Class<T> clazz, String hql, Object... params) {
    Query query = hiber().createQuery(hql);
    for (int i = 0; i < params.length; i++)
      query.setParameter(i, params[i]);
    query.setMaxResults(1);
    return (T) query.uniqueResult();
  }

  @Override
  public <T extends IdEntity> T byCriteria(DetachedCriteria criteria) {
    return (T) criteria.getExecutableCriteria(hiber()).uniqueResult();
  }

  @Override
  public <T extends IdEntity> Map<Long, T> get(Class<T> clazz, Set<Long> ids) {
    List<T> list = (List<T>) hiber().createCriteria(clazz)
        .add(Restrictions.in("id", ids))
        .list();
    Map<Long, T> result = newHashMap();
    for (T e : list)
      result.put(e.id(), e);
    return result;
  }
}
