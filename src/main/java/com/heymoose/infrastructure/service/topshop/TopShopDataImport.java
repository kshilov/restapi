package com.heymoose.infrastructure.service.topshop;

import com.google.inject.Injector;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.infrastructure.service.action.ActionDataImportBase;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import com.heymoose.infrastructure.service.action.ActionDataParser;

public final class TopShopDataImport
    extends ActionDataImportBase<ItemListActionData> {

  public TopShopDataImport(Injector injector) {
    super(injector);
  }

  @Override
  protected Class<? extends ActionDataImporter<ItemListActionData>> importerCls() {
    return TopShopDataImporter.class;
  }

  @Override
  protected Class<? extends ActionDataParser<ItemListActionData>> parserCls() {
    return TopShopActionParser.class;
  }
}
