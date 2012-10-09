package com.heymoose.infrastructure.service.action;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.util.Providers;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.FixPriceActionData;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.infrastructure.service.ImportService;
import com.heymoose.infrastructure.service.ImportServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.net.URL;

public final class ActionDataImportService<T extends ActionData>
    extends ImportServiceBase {


  public static ActionDataImportService<ItemListActionData> basic(Injector inj) {
    return new ActionDataImportService<ItemListActionData>(inj)
        .withImporter(BasicItemListDataImporter.class)
        .withParser(HeymooseItemListParser.class);
  }

  public static ActionDataImportService<ItemListActionData> itemList(
      Injector inj) {
    return new ActionDataImportService<ItemListActionData>(inj);
  }

  public static ActionDataImportService<FixPriceActionData> fix(Injector inj) {
    return new ActionDataImportService<FixPriceActionData>(inj);
  }

  private static final Logger log =
      LoggerFactory.getLogger(ActionDataImportService.class);

  protected final Injector injector;
  protected Class<? extends ActionDataImporter<T>> importerCls;
  protected Class<? extends ActionDataParser<T>> parserCls;
  protected Provider<URL> urlProvider;

  public ActionDataImportService(Injector injector) {
    this.injector = injector;
  }

  @Override
  public final ImportService start() {
    Preconditions.checkNotNull(offerId, "Offer not set.");
    if (urlProvider == null) urlProvider = Providers.of(url);
    log.info("Starting import service: {}.", this);
    ActionDataImporter<T> importer = injector.getInstance(importerCls);
    ActionDataParser<T> parser = injector.getInstance(parserCls);

    ActionDataImportJob<T> importJob = new ActionDataImportJob<T>(
        urlProvider, offerId, importer, parser);

    scheduler.scheduleAtFixedRate(importJob, 0, importPeriod, importTimeUnit);
    return this;
  }

  public ActionDataImportService<T> withImporter(
      Class<? extends ActionDataImporter<T>> importer) {
    this.importerCls = importer;
    return this;
  }

  public ActionDataImportService<T> withParser(
      Class<? extends ActionDataParser<T>> parser) {
    this.parserCls = parser;
    return this;
  }

  public ActionDataImportService<T> withProvider(Provider<URL> provider) {
    this.urlProvider = provider;
    return this;
  }
}
