package com.heymoose.domain.affiliate.counter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;

public abstract class BufferedCounters implements Runnable {

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
  private final int delayMinutes;

  public BufferedCounters(int delayMinutes) {
    this.delayMinutes = delayMinutes;
  }

  private static int incrementAndGetOld(AtomicInteger val) {
    int current;
    do {
      current = val.get();
    } while (!val.compareAndSet(current, current + 1));
    return current;
  }

  public void inc(long key) {
    DelayedCounter newCounter = new DelayedCounter(key, DateTime.now().plusMinutes(delayMinutes));
    DelayedCounter oldCounter = counters.putIfAbsent(key, newCounter);
    if (oldCounter == null) {
      queue.put(newCounter);
    } else {
      // zero means "removed from collections"
      if (incrementAndGetOld(oldCounter.counter) == 0) {
        oldCounter.setExpirationTime(DateTime.now().plusMinutes(delayMinutes));
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
        flushCounter(next.key, next.getAndReset());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public abstract void flushCounter(long key, int diff);
}
