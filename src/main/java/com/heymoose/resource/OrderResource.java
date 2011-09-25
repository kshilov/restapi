package com.heymoose.resource;

import com.google.common.collect.Sets;
import com.heymoose.domain.Account;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.sun.grizzly.util.http.mapper.Mapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Date;

@Path("orders")
@Singleton
public class OrderResource {

  private final UserRepository users;
  private final OrderRepository orders;
  private final OfferRepository offers;

  @Inject
  public OrderResource(UserRepository users, OrderRepository orders, OfferRepository offers) {
    this.users = users;
    this.orders = orders;
    this.offers = offers;
  }

  @GET
  @Transactional
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit) {
    return Response.ok(Mappers.toXmlOrders(orders.list(offset, limit))).build();
  }

  @POST
  @Transactional
  public Response create(@FormParam("userId") int userId,
                         @FormParam("title") String title,
                         @FormParam("body") String body,
                         @FormParam("balance") String balance,
                         @FormParam("cpa") String cpa,
                         @FormParam("autoApprove") @DefaultValue("false") boolean autoApprove) {
    
    BigDecimal decCpa = new BigDecimal(cpa);
    BigDecimal decBalance = new BigDecimal(balance);

    if (decCpa.signum() != 1 || decBalance.signum() != 1)
      return Response.status(400).build();

    if (decCpa.compareTo(decBalance) == 1)
      return Response.status(400).build();

    User user = users.byId(userId);
    if (user == null)
      return Response.status(404).build();
    
    if (user.roles == null || !user.roles.contains(Role.CUSTOMER))
      return Response.status(Response.Status.CONFLICT).build();

    Date now = new Date();

    Offer offer = new Offer();
    offer.title = title;
    offer.type = Offer.Type.URL;
    offer.body = body;
    offer.creationTime = now;
    offer.autoApprove = autoApprove;
    offers.put(offer);

    Order order = new Order();
    order.creationTime = now;
    order.account = new Account();

    BigDecimal amount = new BigDecimal(balance);
    String desc = String.format("Transfering %s from %s to %s", balance, user.customerAccount, order.account);
    user.customerAccount.subtractFromBalance(amount, desc);
    order.account.addToBalance(amount, desc);

    order.offer = offer;
    order.cpa = decCpa;
    order.user = user;
    order.offer = offer;
    orders.put(order);

    offer.order = order;

    if (user.orders == null)
      user.orders = Sets.newHashSet();
    user.orders.add(order);

    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlOrder(order, true)).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response approve(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.approved = true;
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.deleted = true;
    return Response.ok().build();
  }
}
