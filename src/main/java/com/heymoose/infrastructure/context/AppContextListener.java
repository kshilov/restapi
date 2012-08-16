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
import com.heymoose.infrastructure.service.action.ActionDataImportJob;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import com.heymoose.infrastructure.service.action.ActionDataParser;
import com.heymoose.infrastructure.service.action.FixPriceActionDataImporter;
import com.heymoose.infrastructure.service.action.HeymooseFixPriceParser;
import com.heymoose.infrastructure.service.action.HeymooseItemListParser;
import com.heymoose.infrastructure.service.delikateska.DelikateskaDataImporter;
import com.heymoose.infrastructure.service.topshop.TopShopActionParser;
import com.heymoose.infrastructure.service.topshop.TopShopDataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    final ScheduledThreadPoolExecutor topshopExecutor =
        startImportService("topshop", injector,
            TopShopDataImporter.class, TopShopActionParser.class);

    final ScheduledThreadPoolExecutor delikateskaExecutor =
        startImportService("delikateska", injector,
            DelikateskaDataImporter.class, HeymooseItemListParser.class);

    final ScheduledThreadPoolExecutor sapatoExecutor =
        startImportService("sapato", injector,
            FixPriceActionDataImporter.class, HeymooseFixPriceParser.class);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        bufferedShows.flushAll();
        bufferedClicks.flushAll();
        topshopExecutor.shutdown();
        delikateskaExecutor.shutdown();
      }
    });
  }

  @SuppressWarnings("unchecked")
  private static <T extends ActionData> ScheduledThreadPoolExecutor startImportService(
      String shopName, Injector injector,
      Class<? extends ActionDataImporter<T>> importerCls,
      Class<? extends ActionDataParser<T>> parserCls) {

    Properties properties = injector.getInstance(
        Key.get(Properties.class, Names.named("settings")));

    Long parentOfferId = Long.valueOf(
        properties.get(shopName + ".offer").toString());
    Integer importPeriod = Integer.valueOf(
        properties.get(shopName + ".import.period").toString());
    String importUrl = properties.get(shopName + ".import.url").toString();
    final ScheduledThreadPoolExecutor executor =
        new ScheduledThreadPoolExecutor(1);
    ActionDataImporter<T> importer = injector.getInstance(importerCls);
    ActionDataParser<T> parser = injector.getInstance(parserCls);
    ActionDataImportJob<T> importJob =
        new ActionDataImportJob(importUrl, parentOfferId, importer, parser);
    log.info("Starting import service for {}", shopName);
    executor.scheduleAtFixedRate(
        importJob, 0, importPeriod, TimeUnit.MINUTES);
    return executor;
  }
}
