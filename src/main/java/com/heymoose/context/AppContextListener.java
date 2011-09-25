package com.heymoose.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.security.SecurityModule;

public class AppContextListener extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(
            Stage.PRODUCTION,
            new SettingsModule(),
            new JerseyModule(),
            new CommonModule(),
            new SecurityModule(),
            new ProductionModule()
    );
  }
}
