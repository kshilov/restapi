package com.heymoose.infrastructure.service.action;

import com.google.inject.Injector;
import com.google.inject.util.Providers;
import com.heymoose.domain.action.ActionData;

import javax.inject.Provider;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ActionDataImportBase<T extends ActionData>
    implements ActionDataImport<T> {

  private final Injector injector;
  private ScheduledThreadPoolExecutor scheduler;

  protected Long parentOfferId;
  protected Integer periodMinutes;
  protected URL url;

  public ActionDataImportBase(Injector injector) {
    this.injector = injector;
  }


  @Override
  public final ActionDataImport setUrl(String url) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public final ActionDataImport setParentOfferId(Long parentOfferId) {
    this.parentOfferId = parentOfferId;
    return this;
  }

  @Override
  public final ActionDataImport setImportPeriod(Integer minutes) {
    this.periodMinutes = minutes;
    return this;
  }


  @Override
  public final ActionDataImport start() {
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
  public ActionDataImport stop() {
    scheduler.shutdown();
    return this;
  }

  protected Provider<URL> urlProvider() {
    return Providers.of(this.url);
  }

  protected abstract Class<? extends ActionDataImporter<T>> importerCls();
  protected abstract Class<? extends ActionDataParser<T>> parserCls();
}
