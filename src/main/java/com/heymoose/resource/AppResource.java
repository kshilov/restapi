package com.heymoose.resource;

import com.google.common.collect.Sets;
import com.heymoose.domain.Account;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Platform;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.resource.xml.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.UUID;

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

  private void checkNotNull(Object... args) {
    for (Object obj : args)
      if (obj == null)
        throw new WebApplicationException(400);
  }

  @POST
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
    app.account = new Account();
    app.creationTime = new Date();
    // TODO: get from performing
    // app.platform = Platform.valueOf(platform);
    app.secret = UUID.randomUUID().toString();
    app.user = user;

    user.apps.add(app);

    apps.put(app);
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") Long appId) {
    checkNotNull(appId);
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlApp(app, true)).build();
  }

  @PUT
  @Path("{id}")
  public Response regenerateSecret(@PathParam("id") Long appId) {
    checkNotNull(appId);
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    app.secret = UUID.randomUUID().toString();
    return Response.ok().build();
  }
}
