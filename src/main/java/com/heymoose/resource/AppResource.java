package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("apps")
@Singleton
public class AppResource {

  private final UserRepository users;
  private final AppRepository apps;

  @Inject
  public AppResource(UserRepository users, AppRepository apps) {
    this.users = users;
    this.apps = apps;
  }

  @POST
  @Transactional
  public Response create(@FormParam("userId") Long userId) {
    checkNotNull(userId);
    User user = users.byId(userId);
    if (user == null)
      return Response.status(404).build();
    if (!user.isDeveloper())
      return Response.status(Response.Status.CONFLICT).build();

    // only one app now
    if (!user.apps().isEmpty())
      return Response.ok().build();

    App app = new App(user);
    apps.put(app);
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long appId) {
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlApp(app, true)).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response regenerateSecret(@PathParam("id") long appId) {
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    app.regenerateSecret();
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long appId) {
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    app.delete();
    return Response.ok().build();
  }
}
