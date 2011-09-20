package com.heymoose.domain.hiber;

import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class PerformerRepositoryHiber extends RepositoryHiber<Performer> implements PerformerRepository {

  @Inject
  public PerformerRepositoryHiber(Provider<Session> sessionProvider) {
    super(sessionProvider);
  }

  @Override
  public Performer byAppAndExtId(long appId, String extId) {
    return (Performer) hiber()
        .createQuery("from Performer where app.id = :appId and extId = :extId")
        .setParameter("appId", appId)
        .setParameter("extId", extId)
        .uniqueResult();
  }

  @Override
  protected Class<Performer> getEntityClass() {
    return Performer.class;
  }
}
