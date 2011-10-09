package com.heymoose.resource;

import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.events.ActionApproved;
import com.heymoose.events.EventBus;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;

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
import java.math.BigDecimal;

@Path("actions")
@Singleton
public class ActionResource {
  
  private final ActionRepository actions;
  private final EventBus eventBus;
  private final BigDecimal compensation;

  @Inject
  public ActionResource(ActionRepository actions,
                        EventBus eventBus,
                        @Named("compensation") BigDecimal compensation) {
    this.actions = actions;
    this.eventBus = eventBus;
    this.compensation = compensation;
  }
  
  @PUT
  @Path("{id}")
  public Response approve(@PathParam("id") long actionId) {
    eventBus.publish(doApprove(actionId));
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
    if (action.offer().order().deleted())
      action.offer().order().customer().customerAccount().addToBalance(action.reservation().diff().negate(), "Action deleted");
    else
      action.offer().order().account().addToBalance(action.reservation().diff().negate(), "Action deleted");
    action.delete();
    return Response.ok().build();
  }

  @GET
  @Transactional
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("50") int limit) {
    Iterable<Action> page = actions.list(ActionRepository.Ordering.BY_CREATION_TIME_DESC, offset, limit);
    return Response.ok(Mappers.toXmlActions(page)).build();
  }
}
