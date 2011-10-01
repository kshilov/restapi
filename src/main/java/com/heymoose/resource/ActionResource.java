package com.heymoose.resource;

import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Action;
import com.heymoose.domain.ActionRepository;
import com.heymoose.domain.App;
import com.heymoose.domain.Performer;
import com.heymoose.domain.User;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import org.joda.time.DateTime;

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
  @Transactional
  public Response approve(@PathParam("id") Long actionId) {
    Action action = actions.byId(actionId);
    if (action == null)
      return Response.status(404).build();
    if (action.done())
      return Response.ok().build();
    if (action.deleted())
      return Response.status(409).build();
    action.approve();
    return Response.ok().build();
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
