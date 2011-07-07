package com.heymoose.rest.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.rest.domain.order.Order;
import org.hibernate.Session;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

@Path("order")
@Singleton
public class OrderResource {

  private final Provider<Session> sessionProvider;

  @Inject
  public OrderResource(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @POST
  public Response create(@FormParam("name") String name) {
    Order order = new Order(new BigDecimal("0.0"), name);
    hiber().save(order);
    return Response.ok().build();
  }
}
