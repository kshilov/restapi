package com.heymoose.infrastructure.service.action;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.util.Providers;
import com.heymoose.domain.action.ActionData;

import javax.inject.Provider;
import java.net.URL;

public abstract class ActionDataImportService<T extends ActionData>
    extends ImportServiceBase {

  protected final Injector injector;

  public ActionDataImportService(Injector injector) {
    this.injector = injector;
  }

  @Override
  public final ImportService start() {
    Preconditions.checkNotNull(offerId, "Offer not set.");
    ActionDataImporter<T> importer = injector.getInstance(importerCls());
    ActionDataParser<T> parser = injector.getInstance(parserCls());

    ActionDataImportJob<T> importJob = new ActionDataImportJob<T>(
        urlProvider(), offerId, importer, parser);

    scheduler.scheduleAtFixedRate(importJob, 0, importPeriod, importTimeUnit);
    return this;
  }

  protected Provider<URL> urlProvider() {
    return Providers.of(url);
  }

  protected abstract Class<? extends ActionDataImporter<T>> importerCls();
  protected abstract Class<? extends ActionDataParser<T>> parserCls();
}
