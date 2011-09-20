package com.heymoose.domain.hiber;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class AppRepositoryHiber extends RepositoryHiber<App> implements AppRepository {

  @Inject
  public AppRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public App byIdAndSecret(long appId, String secret) {
    return (App) hiber()
        .createQuery("from App where id = :id and secret = :secret")
        .setParameter("id", appId)
        .setParameter("secret", secret)
        .uniqueResult();
  }

  @Override
  protected Class<App> getEntityClass() {
    return App.class;
  }
}
