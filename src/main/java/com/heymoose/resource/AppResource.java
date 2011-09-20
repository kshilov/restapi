package com.heymoose.resource;

import com.google.common.collect.Sets;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Role;
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
import java.util.Date;
import java.util.UUID;

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
    if (user.roles == null || !user.roles.contains(Role.DEVELOPER))
      return Response.status(Response.Status.CONFLICT).build();
    if (user.apps == null)
      user.apps = Sets.newHashSet();
    // only one app now
    if (!user.apps.isEmpty())
      return Response.ok().build();
    App app = new App();
    app.creationTime = new Date();
    app.secret = UUID.randomUUID().toString();
    app.user = user;

    user.apps.add(app);

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
    app.secret = UUID.randomUUID().toString();
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long appId) {
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    app.deleted = true;
    return Response.ok().build();
  }
}
