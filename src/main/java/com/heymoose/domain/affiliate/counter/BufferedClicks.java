package com.heymoose.domain.affiliate.counter;

import com.heymoose.hibernate.Transactional;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Session;

@Singleton
public class BufferedClicks extends BufferedCounters {

  private final static int DEFAULT_DELAY = 10;

  private final Provider<Session> sessionProvider;

  @Inject
  public BufferedClicks(Provider<Session> sessionProvider) {
    super(DEFAULT_DELAY);
    this.sessionProvider = sessionProvider;
  }

  @Override
  @Transactional
  public void flushCounter(long key, int diff) {
    sessionProvider.get()
        .createQuery("update OfferStat set clickCount = clickCount + :diff where id = :id")
        .setParameter("diff", diff)
        .setParameter("id", key)
        .executeUpdate();
  }
}
