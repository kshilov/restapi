package com.heymoose.infrastructure.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.domain.action.ActionData;
import com.heymoose.infrastructure.counter.BufferedClicks;
import com.heymoose.infrastructure.counter.BufferedShows;
import com.heymoose.infrastructure.service.action.BasicItemListImportService;
import com.heymoose.infrastructure.service.action.ImportService;
import com.heymoose.infrastructure.service.carolines.CarolinesImportService;
import com.heymoose.infrastructure.service.sapato.SapatoImportService;
import com.heymoose.infrastructure.service.topshop.TopShopImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import java.util.Properties;

public class AppContextListener extends GuiceServletContextListener {

  private static final Logger log =
      LoggerFactory.getLogger(AppContextListener.class);

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

    startImportService("topshop", properties, new TopShopImportService(injector));

    startImportService("delikateska", properties,
        new BasicItemListImportService(injector));

    startImportService("sapato", properties,
        new SapatoImportService(injector));

    startImportService("trendsbrands", properties,
        new BasicItemListImportService(injector));

    startImportService("shoesbags", properties,
        new BasicItemListImportService(injector));

    startImportService("carolines", properties,
        new CarolinesImportService(injector));

    startImportService("mebelrama", properties,
        new BasicItemListImportService(injector));

    startImportService("domosti", properties,
        new BasicItemListImportService(injector));

    startImportService("babadu", properties,
        new BasicItemListImportService(injector));

    startImportService("juvalia", properties,
        new BasicItemListImportService(injector));

        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
            bufferedShows.flushAll();
            bufferedClicks.flushAll();
          }
        });
  }

  @SuppressWarnings("unchecked")
  private static <T extends ActionData> void startImportService(
      String shopName, Properties properties,
      final ImportService actionImportService) {
    try {
      Long parentOfferId = Long.valueOf(
          properties.get(shopName + ".offer").toString());
      Integer importPeriod = Integer.valueOf(
          properties.get(shopName + ".import.period").toString());
      String url = properties.get(shopName + ".import.url").toString();

      actionImportService.setImportPeriod(importPeriod)
          .setUrl(url)
          .setParentOfferId(parentOfferId);

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
