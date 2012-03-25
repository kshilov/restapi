package com.heymoose.domain.affiliate.hiber;

import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.domain.base.IdEntity;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

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
}
