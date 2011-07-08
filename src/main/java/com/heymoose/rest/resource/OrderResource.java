package com.heymoose.rest.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.order.Targeting;
import com.heymoose.rest.resource.xml.Mappers;
import com.heymoose.rest.resource.xml.XmlOrder;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

@Path("order")
@Singleton
public class OrderResource {

  private final static Logger log = LoggerFactory.getLogger(OrderResource.class);

  private final Provider<Session> sessionProvider;

  @Inject
  public OrderResource(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @POST
  @Transactional
  public Response create(XmlOrder xmlOrder) {
    Order order = new Order(
            new BigDecimal(xmlOrder.balance),
            xmlOrder.name
    );
    Targeting targeting = new Targeting(
            xmlOrder.targeting.age,
            xmlOrder.targeting.male,
            xmlOrder.targeting.city,
            xmlOrder.targeting.country
    );
    order.setTargeting(targeting);
    hiber().save(order);
    return Response.ok(Integer.toString(order.id())).build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") int orderId) {
    Order order = (Order) hiber().get(Order.class, orderId);
    if (order == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    return Response.ok(Mappers.toXmlOrder(order)).build();
  }

  @POST
  @Path("{id}/balance")
  @Transactional
  public Response addToBalance(@PathParam("id") int orderId, @FormParam("amount") String amount) {
    Order order = (Order) hiber().get(Order.class, orderId);
    if (order == null)
      return Response.status(Response.Status.NOT_FOUND).build();
    log.debug("Balance before: " + order.balance().toString());
    try {
      order.addToBalance(new BigDecimal(amount));
    } catch (IllegalArgumentException e) {
      Response.status(Response.Status.BAD_REQUEST).build();
    }
    log.debug("Balance after: " + order.balance().toString());
    return Response.ok().build();
  }
}
