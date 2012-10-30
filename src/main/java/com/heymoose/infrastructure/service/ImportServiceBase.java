package com.heymoose.infrastructure.service;

import com.google.common.base.Objects;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ImportServiceBase implements ImportService {

  protected final ScheduledThreadPoolExecutor scheduler =
      new ScheduledThreadPoolExecutor(1);
  protected Long offerId;
  protected URL url;
  protected Integer importPeriod;
  protected TimeUnit importTimeUnit;

  @Override
  public final ImportService forOffer(Long offerId) {
    this.offerId = offerId;
    return this;
  }

  @Override
  public final ImportService loadDataFromUrl(String url) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public final ImportService loadEvery(Integer period, TimeUnit timeUnit) {
    this.importPeriod = period;
    this.importTimeUnit = timeUnit;
    return this;
  }

  @Override
  public ImportService stop() {
    scheduler.shutdown();
    return this;
  }

  @Override
  public abstract ImportService start();

  @Override
  public String toString() {
      return Objects.toStringHelper(this.getClass())
          .add("url", url)
          .add("period", importPeriod)
          .add("time-unit", importTimeUnit).toString();
  }
}
