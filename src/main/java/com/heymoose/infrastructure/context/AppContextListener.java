package com.heymoose.infrastructure.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.infrastructure.counter.BufferedClicks;
import com.heymoose.infrastructure.counter.BufferedShows;
import javax.servlet.ServletContextEvent;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class AppContextListener extends GuiceServletContextListener {
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
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        bufferedShows.flushAll();
        bufferedClicks.flushAll();
      }
    });
  }
}