package com.heymoose.infrastructure.service.action;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapper;
import com.heymoose.infrastructure.service.yml.YmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class YmlImportService extends ImportServiceBase {

  private static final Logger log =
      LoggerFactory.getLogger(YmlImportService.class);

  private final Injector injector;

  public YmlImportService(Injector injector) {
    this.injector = injector;
  }

  @Override
  public ImportService start() {
    Preconditions.checkNotNull(offerId, "Offer not set.");
    Preconditions.checkNotNull(url, "Yml url not set.");
    final YmlImporter importer = injector.getInstance(YmlImporter.class);
    final YmlCatalogWrapper wrapper = injector.getInstance(ymlWrapperCls());
    Runnable ymlImport = new Runnable() {
      @Override
      public void run() {
        try {
          YmlCatalog originalCatalog = YmlUtil.loadYml(url);
          importer.doImport(wrapper.wrapCatalog(originalCatalog), offerId);
        } catch (Exception e) {
          log.error("Error during yml import.", e);
        }
      }
    };
    scheduler.scheduleAtFixedRate(ymlImport, 0, importPeriod, importTimeUnit);
    return this;
  }

  protected abstract Class<? extends YmlCatalogWrapper> ymlWrapperCls();
}
