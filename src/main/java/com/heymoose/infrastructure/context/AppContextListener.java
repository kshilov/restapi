package com.heymoose.infrastructure.context;

import com.google.common.base.Splitter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.domain.action.SapatoActionData;
import com.heymoose.domain.action.StatusPerItemActionData;
import com.heymoose.infrastructure.counter.BufferedClicks;
import com.heymoose.infrastructure.counter.BufferedShows;
import com.heymoose.infrastructure.service.ImportService;
import com.heymoose.infrastructure.service.action.ActionDataImportService;
import com.heymoose.infrastructure.service.sapato.SapatoParser;
import com.heymoose.infrastructure.service.action.ItemListProductImporter;
import com.heymoose.infrastructure.service.action.StatusPerItemImporter;
import com.heymoose.infrastructure.service.action.StatusPerItemParser;
import com.heymoose.infrastructure.service.carolines.CarolinesActionDataImporter;
import com.heymoose.infrastructure.service.sapato.SapatoImporter;
import com.heymoose.infrastructure.service.sapato.SapatoUrlProvider;
import com.heymoose.infrastructure.service.topshop.TopShopActionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AppContextListener extends GuiceServletContextListener {

  private static final Logger log =
      LoggerFactory.getLogger(AppContextListener.class);
  private static final Splitter splitter = Splitter.on('.');

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(
        Stage.PRODUCTION,
        new SettingsModule(),
        new JerseyModule(),
        new CommonModule(),
        new ResourceModule(),
        new ProductionModule()
    );
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    super.contextInitialized(servletContextEvent);
    SLF4JBridgeHandler.install();
    Injector injector = (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());
    final BufferedShows bufferedShows = injector.getInstance(BufferedShows.class);
    final BufferedClicks bufferedClicks = injector.getInstance(BufferedClicks.class);
    new Thread(bufferedShows).start();
    new Thread(bufferedClicks).start();

    Properties properties = injector.getInstance(
        Key.get(Properties.class, Names.named("settings")));

    startImportService("topshop", properties,
        ActionDataImportService.itemList(injector)
            .withImporter(ItemListProductImporter.class)
            .withParser(TopShopActionParser.class));

    startImportService("delikateska", properties,
        ActionDataImportService.basic(injector));

    try {
    String sapatoBaseUrl = properties.get("sapato.import.url").toString();
    startImportService("sapato", properties,
        new ActionDataImportService<SapatoActionData>(injector)
            .withImporter(SapatoImporter.class)
            .withParser(SapatoParser.class)
            .withProvider(new SapatoUrlProvider(sapatoBaseUrl)));
    } catch (NullPointerException e) {
      // ignore no url
    }

    startImportService("trendsbrands", properties,
        ActionDataImportService.basic(injector));

    startImportService("shoesbags", properties,
        ActionDataImportService.basic(injector));

    startImportService("carolines", properties,
        ActionDataImportService.basic(injector)
            .withImporter(CarolinesActionDataImporter.class));

    startImportService("mebelrama", properties,
        ActionDataImportService.basic(injector));

    startImportService("domosti", properties,
        ActionDataImportService.basic(injector));

    startImportService("babadu", properties,
        ActionDataImportService.basic(injector));

    startImportService("juvalia", properties,
        ActionDataImportService.basic(injector));

    startImportService("elitdress", properties,
        new ActionDataImportService<StatusPerItemActionData>(injector)
            .withImporter(StatusPerItemImporter.class)
            .withParser(StatusPerItemParser.class));

        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
            bufferedShows.flushAll();
            bufferedClicks.flushAll();
          }
        });
  }

  @SuppressWarnings("unchecked")
  private static void startImportService(
      String shopName, Properties properties,
      final ImportService actionImportService) {
    try {
      String shopOnly = splitter.split(shopName).iterator().next();
      Long parentOfferId = Long.valueOf(
          properties.get(shopOnly + ".offer").toString());
      Integer importPeriod = Integer.valueOf(
          properties.get(shopName + ".import.period").toString());
      String url = properties.get(shopName + ".import.url").toString();

      actionImportService.forOffer(parentOfferId)
          .loadDataFromUrl(url)
          .loadEvery(importPeriod, TimeUnit.MINUTES);

      log.info("Starting import service for {}", shopName);
      actionImportService.start();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          actionImportService.stop();
        }
      });

    } catch (Exception e) {
      log.error("Could not start import service for " + shopName, e);
    }
  }
}
