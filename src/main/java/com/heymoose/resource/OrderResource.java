package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.Offer;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.base.Repository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;
import com.heymoose.util.WebAppUtil;
import org.joda.time.DateTime;

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

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("orders")
@Singleton
public class OrderResource {

  private final UserRepository users;
  private final OrderRepository orders;
  private final Accounts accounts;

  @Inject
  public OrderResource(UserRepository users, OrderRepository orders, Accounts accounts) {
    this.users = users;
    this.orders = orders;
    this.accounts = accounts;
  }

  @GET
  @Transactional
  public Response list(@QueryParam("ord") @DefaultValue("creation-time") String ord,
                       @QueryParam("dir") @DefaultValue("desc") String dir,
                       @QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Response.ok(Mappers.toXmlOrders(orders.list(
        WebAppUtil.queryParamToEnum(ord, OrderRepository.Ordering.CREATION_TIME),
        WebAppUtil.queryParamToEnum(dir, Repository.Direction.DESC),
        offset, limit, 0), d)).build();
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count() {
    return Response.ok(Mappers.toXmlCount(orders.count())).build();
  }

  @POST
  @Transactional
  public Response create(@FormParam("userId") int userId,
                         @FormParam("title") String title,
                         @FormParam("description") String description,
                         @FormParam("body") String body,
                         @FormParam("image") String image,
                         @FormParam("balance") String _balance,
                         @FormParam("cpa") String _cpa,
                         @FormParam("autoApprove") @DefaultValue("false") boolean autoApprove) {
    checkNotNull(title, description, body, image, _balance, _balance);

    BigDecimal cpa = new BigDecimal(_cpa);
    BigDecimal balance = new BigDecimal(_balance);

    if (cpa.signum() != 1 || balance.signum() != 1)
      return Response.status(400).build();

    if (cpa.compareTo(balance) == 1)
      return Response.status(400).build();

    User user = users.byId(userId);
    if (user == null)
      return Response.status(404).build();
    
    if (!user.isCustomer())
      return Response.status(Response.Status.CONFLICT).build();

    DateTime now = DateTime.now();
    Offer offer = new Offer(title, description, body, image, autoApprove, now);
    Order order = new Order(offer, cpa, user, now);
    
    BigDecimal amount = balance;

    if (user.customerAccount().currentState().balance().compareTo(amount) == -1)
      return Response.status(409).build();

    String desc = String.format("Transfering %s from %s to %s", _balance, user.customerAccount(), order.account());
    accounts.lock(user.customerAccount());
    user.customerAccount().subtractFromBalance(amount, desc);
    order.account().addToBalance(amount, desc);

    orders.put(order);
    return Response.ok(Long.toString(order.id())).build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlOrder(order, Details.WITH_RELATED_ENTITIES)).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response approve(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.approve();
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.delete();
    return Response.ok().build();
  }
}
