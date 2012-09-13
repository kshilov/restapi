package com.heymoose.infrastructure.service.carolines;

import com.google.inject.Injector;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import com.heymoose.infrastructure.service.action.ActionDataParser;
import com.heymoose.infrastructure.service.action.HeymooseItemListParser;
import com.heymoose.infrastructure.service.action.ImportServiceBase;

public final class CarolinesImportService
    extends ImportServiceBase<ItemListActionData> {

  public CarolinesImportService(Injector injector) {
    super(injector);
  }

  @Override
  protected Class<? extends ActionDataImporter<ItemListActionData>> importerCls() {
    return CarolinesActionDataImporter.class;
  }

  @Override
  protected Class<? extends ActionDataParser<ItemListActionData>> parserCls() {
    return HeymooseItemListParser.class;
  }
}