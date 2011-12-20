package com.heymoose.resource;

import static com.heymoose.util.WebAppUtil.checkNotNull;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.Order;
import com.heymoose.events.ActionApproved;
import com.heymoose.events.EventBus;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import java.math.BigDecimal;

@Path("actions")
@Singleton
public class ActionResource {
  
  private final ActionRepository actions;
//  private final EventBus eventBus;
  private final BigDecimal compensation;
  private final Accounts accounts;

  @Inject
  public ActionResource(ActionRepository actions,
//                        EventBus eventBus,
                        @Named("compensation") BigDecimal compensation,
                        Accounts accounts) {
    this.actions = actions;
//    this.eventBus = eventBus;
    this.compensation = compensation;
    this.accounts = accounts;
  }
  
  @PUT
  @Path("{id}")
  public Response approve(@PathParam("id") long actionId) {
//    eventBus.publish(doApprove(actionId));
    doApprove(actionId);
    return Response.ok().build();
  }

  @Transactional
  public ActionApproved doApprove(long actionId) {
    Action action = actions.byId(actionId);
    if (action == null)
      throw new WebApplicationException(404);
    if (action.done())
      return new ActionApproved(action, compensation);
    if (action.deleted())
      throw new WebApplicationException(409);
    accounts.lock(action.app().owner().developerAccount());
    action.approve(compensation);
    return new ActionApproved(action, compensation);
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long actionId) {
    Action action = actions.byId(actionId);
    if (action == null)
      return Response.status(404).build();
    if (action.deleted())
      return Response.ok().build();
    Order order = action.offer().order();
    if (order.disabled()) {
      accounts.lock(order.customer().customerAccount());
      order.customer().customerAccount().addToBalance(action.reservedAmount(), "Action deleted");
    } else {
      accounts.lock(order.account());
      order.account().addToBalance(action.reservedAmount(), "Action deleted");
    }
    action.delete();
    return Response.ok().build();
  }
  
  @GET
  @Path("range")
  @Transactional
  public Response list(@QueryParam("from") Long from,
                       @QueryParam("to") Long to,
                       @QueryParam("offerId") Long offerId,
                       @QueryParam("appId") Long appId,
                       @QueryParam("performerId") Long performerId) {
    checkNotNull(from, to);
    DateTime dtFrom = new DateTime(from * 1000);
    DateTime dtTo = new DateTime(to * 1000);
    return Response.ok(Mappers.toXmlActions(
        actions.list(dtFrom, dtTo, offerId, appId, performerId), Details.ONLY_ENTITY)
    ).build();
  }

  @GET
  @Transactional
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("50") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full,
                       @QueryParam("offerId") Long offerId,
                       @QueryParam("appId") Long appId,
                       @QueryParam("performerId") Long performerId) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    Iterable<Action> page = actions.list(ActionRepository.Ordering.BY_CREATION_TIME_DESC, offset, limit,
        offerId, appId, performerId);
    return Response.ok(Mappers.toXmlActions(page, d)).build();
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count(@QueryParam("offerId") Long offerId,
                        @QueryParam("appId") Long appId,
                        @QueryParam("performerId") Long performerId) {
    return Response.ok(Mappers.toXmlCount(actions.count(offerId, appId, performerId))).build();
  }
  
  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long actionId) {
    Action action = actions.byId(actionId);
    if (action == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlAction(action, Details.WITH_RELATED_ENTITIES)).build();
  }
}
