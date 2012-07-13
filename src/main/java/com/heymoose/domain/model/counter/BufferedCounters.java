package com.heymoose.domain.model.counter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BufferedCounters implements Runnable {

  private final static Logger log = LoggerFactory.getLogger(BufferedCounters.class);

  private static class DelayedCounter implements Delayed {

    private final long key;
    private final AtomicInteger counter = new AtomicInteger(1);
    private volatile DateTime expirationTime;

    private DelayedCounter(long key, DateTime expirationTime) {
      this.key = key;
      this.expirationTime = expirationTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      long delay = expirationTime.getMillis() - DateTime.now().getMillis();
      return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
      DelayedCounter counter = (DelayedCounter) o;
      return this.expirationTime.compareTo(counter.expirationTime);
    }

    public int getAndReset() {
      return counter.getAndSet(0);
    }

    public void setExpirationTime(DateTime expirationTime) {
      this.expirationTime = expirationTime;
    }
  }

  private final ConcurrentMap<Long, DelayedCounter> counters = new ConcurrentHashMap<Long, DelayedCounter>();
  private final DelayQueue<DelayedCounter> queue = new DelayQueue<DelayedCounter>();
  private final int delaySeconds;

  public BufferedCounters(int delaySeconds) {
    this.delaySeconds = delaySeconds;
  }

  private static int getAndIncrement(AtomicInteger val) {
    int current;
    do {
      current = val.get();
    } while (!val.compareAndSet(current, current + 1));
    return current;
  }

  public void inc(long key) {
    DelayedCounter newCounter = new DelayedCounter(key, DateTime.now().plusSeconds(delaySeconds));
    DelayedCounter oldCounter = counters.putIfAbsent(key, newCounter);
    if (oldCounter == null) {
      queue.put(newCounter);
    } else {
      // zero means "removed from collections"
      if (getAndIncrement(oldCounter.counter) == 0) {
        oldCounter.setExpirationTime(DateTime.now().plusSeconds(delaySeconds));
        counters.put(key, oldCounter);
        queue.put(oldCounter);
      }
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        DelayedCounter next = queue.take();
        counters.remove(next.key);
        flushCounterSafely(next.key, next.getAndReset());
      }
    } catch (InterruptedException e) {
      log.warn("Buffer was interrupted", e);
    }
  }

  public void flushAll() {
    for (DelayedCounter counter : queue) {
      queue.remove(counter);
      counters.remove(counter.key);
      flushCounterSafely(counter.key, counter.getAndReset());
    }
  }

  private void flushCounterSafely(long key, int diff) {
    try {
      flushCounter(key, diff);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public abstract void flushCounter(long key, int diff);
}
