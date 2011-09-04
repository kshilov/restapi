package com.heymoose.rest.test.base;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.heymoose.context.CommonModule;
import com.heymoose.context.JerseyModule;
import org.junit.Ignore;

@Ignore
public class TestContextListener extends GuiceServletContextListener {

  private volatile static Injector injector;

  @Override
  protected Injector getInjector() {
    if (injector != null)
      return injector;
    return (injector = Guice.createInjector(
            new JerseyModule(),
            new CommonModule(),
            new TestModule()//,
            //new SecurityModule()
    ));
  }

  public static Injector injector() {
    return injector;
  }
}
