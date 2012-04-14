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
  private final ConcurrentHashMap<Long, AtomicInteger> clicks = new ConcurrentHashMap<Long, AtomicInteger>();

  private final Provider<Session> sessionProvider;

  @Inject
  public OfferStatBuffer(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  public void incShows(long offerStatId) {
    shows.putIfAbsent(offerStatId, new AtomicInteger());
    int cnt = shows.get(offerStatId).incrementAndGet();
    if (cnt >= THRESHOLD)
      flushShows();
  }

  public void incClicks(long offerStatId) {
    clicks.putIfAbsent(offerStatId, new AtomicInteger());
    int cnt = clicks.get(offerStatId).incrementAndGet();
    if (cnt >= THRESHOLD)
      flushClicks();
  }

  @Transactional
  public void flushShows() {
    Session s = sessionProvider.get();
    Query query = s.createQuery("update OfferStat set showCount = showCount + :diff where id = :id");
    for (Map.Entry<Long, AtomicInteger> ent : shows.entrySet()) {
      long shows = ent.getValue().getAndSet(0);
      query.setParameter("diff", shows);
      query.setParameter("id", ent.getKey());
      query.executeUpdate();
    }
  }

  @Transactional
  public void flushClicks() {
    Session s = sessionProvider.get();
    Query query = s.createQuery("update OfferStat set clickCount = clickCount + :diff where id = :id");
    for (Map.Entry<Long, AtomicInteger> ent : clicks.entrySet()) {
      long shows = ent.getValue().getAndSet(0);
      query.setParameter("diff", shows);
      query.setParameter("id", ent.getKey());
      query.executeUpdate();
    }
  }
}
