package com.heymoose.infrastructure.context;

import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class JerseyModule extends JerseyServletModule {

  @Override
  protected void configureServlets() {
    serve("*").with(GuiceContainer.class);
  }
}
