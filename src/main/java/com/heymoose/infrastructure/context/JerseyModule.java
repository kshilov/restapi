package com.heymoose.infrastructure.context;

import com.heymoose.resource.xml.JDomElementWriter;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class JerseyModule extends JerseyServletModule {

  @Override
  protected void configureServlets() {
//    ImmutableMap<String, String> initParams = ImmutableMap.of(
//        "com.sun.jersey.spi.container.ContainerRequestFilters",
//        "com.sun.jersey.api.container.filter.LoggingFilter");
    bind(JDomElementWriter.class);
    serve("*").with(GuiceContainer.class);
  }
}
