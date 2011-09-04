package com.heymoose.resource;

import com.google.common.collect.Sets;
import com.heymoose.domain.Account;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Platform;
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
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Path("app")
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
  public Response create(@FormParam("userId") long userId, @FormParam("platform") String platform) {
    User user = users.get(userId);
    if (user == null)
      return Response.status(404).build();
    if (user.apps == null)
      user.apps = Sets.newHashSet();
    // only one app now
    if (!user.apps.isEmpty())
      return Response.ok().build();
    App app = new App();
    app.account = new Account();
    app.creationTime = new Date();
    app.platform = Platform.valueOf(platform);
    app.secret = UUID.randomUUID().toString();

    user.apps.add(app);
    apps.put(app);
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") long appId) {
    App app = apps.get(appId);
    if (app == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlApp(app)).build();
  }
}
