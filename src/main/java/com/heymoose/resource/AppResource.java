package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Platform;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;

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
import java.net.URI;

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
  
  @GET
  @Transactional
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Response.ok(Mappers.toXmlApps(apps.list(offset, limit), d)).build();
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count() {
    return Response.ok(Mappers.toXmlCount(apps.count())).build();
  }

  @POST
  @Transactional
  public Response create(@FormParam("title") String title,
                         @FormParam("userId") Long userId,
                         @FormParam("url") String url,
                         @FormParam("callback") String callback,
                         @FormParam("platform") Platform platform) {
    checkNotNull(title, userId, url, callback, platform);
    User user = users.byId(userId);
    if (user == null)
      return Response.status(404).build();
    if (!user.isDeveloper())
      return Response.status(Response.Status.CONFLICT).build();

    App app = new App(title, user, URI.create(url), URI.create(callback), platform);
    apps.put(app);
    return Response.ok(Long.toString(app.id())).build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long appId) {
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlApp(app, Details.WITH_RELATED_ENTITIES)).build();
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
