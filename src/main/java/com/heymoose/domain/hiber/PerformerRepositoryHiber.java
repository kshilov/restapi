package com.heymoose.domain.hiber;

import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
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
  public Performer byPlatformAndExtId(Platform platform, String extId) {
    return (Performer) hiber()
         .createQuery("from Performer where platform = :platform and extId = :extId")
         .setParameter("platform", platform)
         .setParameter("extId", extId)
         .uniqueResult();
  }

  @Override
  protected Class<Performer> getEntityClass() {
    return Performer.class;
  }
}
