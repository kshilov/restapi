package com.heymoose.resource;

import com.google.common.collect.Sets;
import com.heymoose.domain.Account;
import com.heymoose.domain.Action;
import com.heymoose.domain.Offer;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.resource.xml.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

@Path("order")
@Singleton
public class OrderResource {

  private final UserRepository users;
  private final OrderRepository orders;

  @Inject
  public OrderResource(UserRepository users, OrderRepository orders) {
    this.users = users;
    this.orders = orders;
  }

  @POST
  @Path("url")
  public Response create(@FormParam("userId") int userId, @FormParam("title") String title, @FormParam("url") String url, @FormParam("balance") String balance) {
    User user = users.get(userId);
    if (user == null)
      return Response.status(404).build();
    Date now = new Date();

    Action action = new Action();
    action.title = title;
    action.type = Action.ActionType.URL_ACTION;
    action.body = url;
    action.creationTime = now;

    Offer offer = new Offer();
    offer.action = action;

    Order order = new Order();
    order.creationTime = now;
    order.account = new Account(new BigDecimal(balance));
    order.offer = offer;
    orders.put(order);

    if (user.orders == null)
      user.orders = Sets.newHashSet();

    user.orders.add(order);
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") long orderId) {
    Order order = orders.get(orderId);
    if (order == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlOrder(order)).build();
  }

  @POST
  @Path("action")
  public Response perform() {
    return null;
  }
}
