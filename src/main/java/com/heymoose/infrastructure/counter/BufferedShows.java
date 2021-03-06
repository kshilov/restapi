package com.heymoose.infrastructure.counter;

import com.heymoose.infrastructure.persistence.Transactional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class BufferedShows extends BufferedCounters {

  private final static int DEFAULT_DELAY = 10;

  private final Provider<Session> sessionProvider;

  @Inject
  public BufferedShows(Provider<Session> sessionProvider) {
    super(DEFAULT_DELAY);
    this.sessionProvider = sessionProvider;
  }

  @Override
  @Transactional
  public void flushCounter(long key, int diff) {
    sessionProvider.get()
        .createQuery("update OfferStat set showCount = showCount + :diff where id = :id")
        .setParameter("diff", (long) diff)
        .setParameter("id", key)
        .executeUpdate();
  }
}
