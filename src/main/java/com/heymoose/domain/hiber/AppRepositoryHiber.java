package com.heymoose.domain.hiber;

import com.google.common.collect.Sets;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.hibernate.Transactional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class AppRepositoryHiber extends RepositoryHiber<App> implements AppRepository {

  @Inject
  public AppRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Set<App> all() {
    return Sets.newHashSet(hiber().createQuery("from App where deleted = false order by creationTime desc").list());
  }

  @Override
  @Transactional
  public App byId(long id) {
    return (App) hiber().createQuery("from App where id = :id and deleted = false")
        .setParameter("id", id)
        .uniqueResult();
  }

  @Override
  protected Class<App> getEntityClass() {
    return App.class;
  }

  @Override
  public App anyById(long appId) {
    return super.byId(appId);
  }

  @Override
  public Iterable<App> list(int offset, int limit) {
    return hiber()
        .createQuery("from App order by creationTime desc")
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }
  
  @Override
  public long count() {
    return Long.parseLong(hiber()
        .createQuery("select count(*) from App")
        .uniqueResult()
        .toString());
  }
}
