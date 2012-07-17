package com.heymoose.infrastructure.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.topshop.TopShopDataImporter;
import com.heymoose.infrastructure.service.topshop.TopShopImportJob;
import org.joda.time.DateTime;

import java.util.Map;

public final class TopShopImport {

  private static class TopShopOffersModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    public Map<String, Long> topShopOfferMap() {
      return ImmutableMap.of(
          "hm_1_item_1", 1L,
          "hm_1_item_2", 2L);

    }
  }

  public static void main(String... args) throws Exception {
    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule(),
        new TopShopOffersModule());
    TopShopDataImporter importer = injector.getInstance(TopShopDataImporter.class);
    TopShopImportJob job = new TopShopImportJob(
        "http://localhost:8000/example.xml", importer);
    job.run(DateTime.now());
  }
}
