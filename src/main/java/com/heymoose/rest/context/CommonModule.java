package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.heymoose.rest.domain.order.Orders;
import com.heymoose.rest.domain.question.Questions;
import com.heymoose.rest.domain.security.Secured;
import com.heymoose.rest.domain.security.SecuredInterceptor;
import com.heymoose.rest.resource.ApiResource;
import com.heymoose.rest.resource.AppResource;
import com.heymoose.rest.resource.OrderResource;

public class CommonModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HibernateModule());
    bind(AppResource.class);
    bind(OrderResource.class);
    bind(ApiResource.class);
    bind(Orders.class);
    bind(Questions.class);
  }
}
