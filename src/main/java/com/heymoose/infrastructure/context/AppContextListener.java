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
import com.heymoose.infrastructure.service.action.ActionDataImport;
import com.heymoose.infrastructure.service.delikateska.DelikateskaActionDataImport;
import com.heymoose.infrastructure.service.sapato.SapatoActionDataImport;
import com.heymoose.infrastructure.service.topshop.TopShopDataImport;
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

    startImportService("topshop", properties, new TopShopDataImport(injector));

    startImportService("delikateska", properties,
        new DelikateskaActionDataImport(injector));

    startImportService("sapato", properties,
        new SapatoActionDataImport(injector));

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
      final ActionDataImport<T> actionImport) {
    try {
      Long parentOfferId = Long.valueOf(
          properties.get(shopName + ".offer").toString());
      Integer importPeriod = Integer.valueOf(
          properties.get(shopName + ".import.period").toString());
      String url = properties.get(shopName + ".import.url").toString();

      actionImport.setImportPeriod(importPeriod)
          .setUrl(url)
          .setParentOfferId(parentOfferId);

      log.info("Starting import service for {}", shopName);
      actionImport.start();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          actionImport.stop();
        }
      });

    } catch (Exception e) {
      log.error("Could not start import service for " + shopName, e);
    }
  }
}
