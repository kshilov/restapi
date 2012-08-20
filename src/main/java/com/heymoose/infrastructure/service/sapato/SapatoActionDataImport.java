package com.heymoose.infrastructure.service.sapato;

import com.google.inject.Injector;
import com.heymoose.domain.action.FixPriceActionData;
import com.heymoose.infrastructure.service.action.ActionDataImportBase;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import com.heymoose.infrastructure.service.action.ActionDataParser;
import com.heymoose.infrastructure.service.action.FixPriceActionDataImporter;
import com.heymoose.infrastructure.service.action.HeymooseFixPriceParser;

import javax.inject.Provider;
import java.net.URL;

public final class SapatoActionDataImport
    extends ActionDataImportBase<FixPriceActionData> {

  public SapatoActionDataImport(Injector injector) {
    super(injector);
  }

  @Override
  protected Class<? extends ActionDataImporter<FixPriceActionData>> importerCls() {
    return FixPriceActionDataImporter.class;
  }

  @Override
  protected Class<? extends ActionDataParser<FixPriceActionData>> parserCls() {
    return HeymooseFixPriceParser.class;
  }

  @Override
  protected Provider<URL> urlProvider() {
    return new SapatoUrlProvider(url);
  }
}
