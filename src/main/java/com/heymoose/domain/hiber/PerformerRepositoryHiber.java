package com.heymoose.domain.hiber;

import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.domain.Platform;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

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
  
  @Override
  public Iterable<Performer> list(int offset, int limit) {
    return hiber()
        .createQuery("from Performer order by creationTime desc")
        .setFirstResult(offset)
        .setMaxResults(limit)
        .list();
  }
  
  @Override
  public long count() {
    return Long.parseLong(hiber()
        .createQuery("select count(*) from Performer")
        .uniqueResult()
        .toString());
  }
}
