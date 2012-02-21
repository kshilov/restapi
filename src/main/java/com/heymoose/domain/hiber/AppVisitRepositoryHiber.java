package com.heymoose.domain.hiber;

import com.heymoose.domain.App;
import com.heymoose.domain.AppVisit;
import com.heymoose.domain.AppVisitRepository;
import com.heymoose.domain.Performer;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class AppVisitRepositoryHiber extends RepositoryHiber<AppVisit> implements AppVisitRepository {

  @Inject
  public AppVisitRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  protected Class<AppVisit> getEntityClass() {
    return AppVisit.class;
  }

  @Override
  public AppVisit byVisitorAppAnd(Performer visitor, App app) {
    return (AppVisit) hiber().createQuery("from AppVisit where app = :app and visitor = :visitor")
        .setParameter("app", app)
        .setParameter("visitor", visitor)
        .setMaxResults(1)
        .uniqueResult();
  }
}
