package com.heymoose.infrastructure.service.action;

import com.google.inject.Injector;
import com.heymoose.domain.action.ItemListActionData;

public final class BasicItemListImportService
    extends ActionDataImportService<ItemListActionData> {

  public BasicItemListImportService(Injector injector) {
    super(injector);
  }

  @Override
  protected Class<? extends ActionDataImporter<ItemListActionData>> importerCls() {
    return BasicItemListDataImporter.class;
  }

  @Override
  protected Class<? extends ActionDataParser<ItemListActionData>> parserCls() {
    return HeymooseItemListParser.class;
  }
}
