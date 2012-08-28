package com.heymoose.infrastructure.service.trendsbrands;

import com.google.inject.Injector;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import com.heymoose.infrastructure.service.action.ActionDataParser;
import com.heymoose.infrastructure.service.action.BasicItemListDataImporter;
import com.heymoose.infrastructure.service.action.HeymooseItemListParser;
import com.heymoose.infrastructure.service.action.ImportServiceBase;
import com.heymoose.infrastructure.service.action.ItemListActionDataImporter;

public final class TrendsBrandsImportService
    extends ImportServiceBase<ItemListActionData> {

  public TrendsBrandsImportService(Injector injector) {
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
