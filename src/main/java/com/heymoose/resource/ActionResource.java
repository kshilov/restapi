package com.heymoose.resource;

import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.resource.xml.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("actions")
@Singleton
public class ActionResource {
  
  private final ActionRepository actions;

  @Inject
  public ActionResource(ActionRepository actions) {
    this.actions = actions;
  }

  @PUT
  @Path("{id}")
  public Response approve(@PathParam("id") Long actionId) {
    Action action = actions.byId(actionId);
    if (action == null)
      return Response.status(404).build();
    action.done = true;
    action.performer.app.user.developerAccount.addToBalance(action.reservation.diff().negate(), "Action approved");
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long actionId) {
    Action action = actions.byId(actionId);
    if (action == null)
      return Response.status(404).build();
    if (action.deleted)
      return Response.ok().build();
    if (action.offer.order.deleted)
      action.offer.order.user.customerAccount.addToBalance(action.reservation.diff().negate(), "Action deleted");
    else
      action.offer.order.account.addToBalance(action.reservation.diff().negate(), "Action deleted");
    action.deleted = true;
    return Response.ok().build();
  }

  @GET
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("50") int limit) {
    Iterable<Action> page = actions.list(ActionRepository.Ordering.BY_CREATION_TIME_DESC, offset, limit);
    return Response.ok(Mappers.toXmlActions(page)).build();
  }
}
