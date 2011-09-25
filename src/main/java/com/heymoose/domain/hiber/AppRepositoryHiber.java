package com.heymoose.domain.hiber;

import com.google.common.collect.Sets;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class AppRepositoryHiber extends RepositoryHiber<App> implements AppRepository {

  @Inject
  public AppRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Set<App> all() {
    return Sets.newHashSet(hiber().createQuery("from App where deleted = false").list());
  }

  @Override
  public App byId(long id) {
    return (App) hiber().createQuery("from App where id = :id and deleted = false")
        .setParameter("id", id)
        .uniqueResult();
  }

  @Override
  protected Class<App> getEntityClass() {
    return App.class;
  }
}
