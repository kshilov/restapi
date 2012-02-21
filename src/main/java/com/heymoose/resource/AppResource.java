package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Platform;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;
import com.heymoose.resource.xml.XmlApps;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.net.URI;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("apps")
@Singleton
public class AppResource {

  private final UserRepository users;
  private final AppRepository apps;
  private final Accounts accounts;

  @Inject
  public AppResource(UserRepository users, AppRepository apps, Accounts accounts) {
    this.users = users;
    this.apps = apps;
    this.accounts = accounts;
  }
  
  @GET
  @Transactional
  public XmlApps list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full,
                       @QueryParam("withDeleted") @DefaultValue("false") boolean withDeleted,
                       @QueryParam("userId") Long userId,
                       @QueryParam("maxD") Double maxD) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Mappers.toXmlApps(accounts, apps.list(offset, limit, userId, maxD, withDeleted), d);
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count(@QueryParam("withDeleted") @DefaultValue("false") boolean withDeleted,
                        @QueryParam("userId") Long userId,
                        @QueryParam("maxD") Double maxD) {
    return Response.ok(Mappers.toXmlCount(apps.count(userId, maxD, withDeleted))).build();
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
    App app = apps.anyById(appId);
    if (app == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlApp(accounts, app, Details.WITH_RELATED_ENTITIES)).build();
  }
  
  @PUT
  @Path("{id}")
  @Transactional
  public void update(@PathParam("id") long appId,
                     @FormParam("title") String title,
                     @FormParam("url") String url,
                     @FormParam("callback") String callback,
                     @FormParam("platform") Platform platform,
                     @FormParam("deleted") Boolean deleted,
                     @FormParam("d") Double d,
                     @FormParam("t") Double t) {
    App app = apps.anyById(appId);
    if (app == null)
      throw new WebApplicationException(404);
    
    if (title != null) app.setTitle(title);
    if (url != null) app.setUrl(URI.create(url));
    if (callback != null) app.setCallback(URI.create(callback));
    if (platform != null) app.setPlatform(platform);
    if (deleted != null) app.setDeleted(deleted);
    if (d != null) app.setD(new BigDecimal(d));
    if (t != null) app.setT(new BigDecimal(t));
  }

  @PUT
  @Path("{id}/secret")
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
