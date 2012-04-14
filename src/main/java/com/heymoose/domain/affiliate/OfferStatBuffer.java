package com.heymoose.domain.affiliate;

import com.heymoose.hibernate.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.Query;
import org.hibernate.Session;

@Singleton
public class OfferStatBuffer {

  private final static int THRESHOLD = 100;

  private final ConcurrentHashMap<Long, AtomicInteger> shows = new ConcurrentHashMap<Long, AtomicInteger>();
  private final AtomicInteger counter = new AtomicInteger();

  private final Provider<Session> sessionProvider;

  @Inject
  public OfferStatBuffer(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  public void incShows(long offerStatId) {
    shows.putIfAbsent(offerStatId, new AtomicInteger());
    shows.get(offerStatId).incrementAndGet();
    counter.incrementAndGet();
    tryFlush();
  }

  public void tryFlush() {
    if (counter.get() >= THRESHOLD) {
      flush();
      counter.set(0);
    }
  }

  @Transactional
  public void flush() {
    Session s = sessionProvider.get();
    Query query = s.createQuery("update OfferStat set showCount = showCount + :diff where id = :id");
    for (Map.Entry<Long, AtomicInteger> ent : shows.entrySet()) {
      long shows = ent.getValue().getAndSet(0);
      query.setParameter("diff", shows);
      query.setParameter("id", ent.getKey());
      query.executeUpdate();
    }
  }
}
