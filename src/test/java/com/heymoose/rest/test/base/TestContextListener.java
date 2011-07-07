package com.heymoose.rest.test.base;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.rest.context.CommonModule;
import com.heymoose.rest.context.JerseyModule;
import com.heymoose.rest.context.SettingsModule;
import org.junit.Ignore;

@Ignore
public class TestContextListener extends GuiceServletContextListener {

  private static Injector injector;

  @Override
  protected Injector getInjector() {
    return (injector = Guice.createInjector(
            new SettingsModule(),
            new JerseyModule(),
            new CommonModule(),
            new TestModule()
    ));
  }

  public static Injector injector() {
    return injector;
  }
}
