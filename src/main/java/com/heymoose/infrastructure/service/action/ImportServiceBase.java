package com.heymoose.infrastructure.service.action;

import com.google.inject.Injector;
import com.google.inject.util.Providers;
import com.heymoose.domain.action.ActionData;

import javax.inject.Provider;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ImportServiceBase<T extends ActionData>
    implements ImportService<T> {

  private final Injector injector;
  private ScheduledThreadPoolExecutor scheduler;

  protected Long parentOfferId;
  protected Integer periodMinutes;
  protected URL url;

  public ImportServiceBase(Injector injector) {
    this.injector = injector;
  }


  @Override
  public final ImportService setUrl(String url) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public final ImportService setParentOfferId(Long parentOfferId) {
    this.parentOfferId = parentOfferId;
    return this;
  }

  @Override
  public final ImportService setImportPeriod(Integer minutes) {
    this.periodMinutes = minutes;
    return this;
  }


  @Override
  public final ImportService start() {
    scheduler = new ScheduledThreadPoolExecutor(1);
    ActionDataImporter<T> importer = injector.getInstance(importerCls());
    ActionDataParser<T> parser = injector.getInstance(parserCls());

    ActionDataImportJob<T> importJob = new ActionDataImportJob<T>(
        urlProvider(), this.parentOfferId, importer, parser);

    scheduler.scheduleAtFixedRate(
        importJob, 0, this.periodMinutes, TimeUnit.MINUTES);

    return this;
  }

  @Override
  public ImportService stop() {
    scheduler.shutdown();
    return this;
  }

  protected Provider<URL> urlProvider() {
    return Providers.of(this.url);
  }

  protected abstract Class<? extends ActionDataImporter<T>> importerCls();
  protected abstract Class<? extends ActionDataParser<T>> parserCls();
}
