package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.heymoose.rest.resource.AppResource;
import com.heymoose.rest.resource.OrderResource;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HibernateModule());
    bind(AppResource.class);
    bind(OrderResource.class);
  }
}
