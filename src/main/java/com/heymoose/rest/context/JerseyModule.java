package com.heymoose.rest.context;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class JerseyModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("*").with(GuiceContainer.class);
  }
}
