package com.heymoose.rest.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class AppContextListener extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(
            new SettingsModule(),
            new JerseyModule(),
            new CommonModule(),
            new ProductionModule()
    );
  }
}
