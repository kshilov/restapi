package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Singleton
@Path("performers")
public class PerformerResource {

  private final AppRepository apps;
  private final PerformerRepository performers;

  @Inject
  public PerformerResource(AppRepository apps, PerformerRepository performers) {
    this.apps = apps;
    this.performers = performers;
  }
  
  @GET
  @Transactional
  public Response list(@QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Response.ok(Mappers.toXmlPerformers(performers.list(offset, limit), d)).build();
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count() {
    return Response.ok(Mappers.toXmlCount(performers.count())).build();
  }

  @POST
  @Transactional
  public Response create(@FormParam("extId") String extId,
                         @FormParam("inviterExtId") String inviterExtId,
                         @FormParam("appId") long appId) {
    App app = apps.byId(appId);
    if (app == null)
      return Response.status(404).build();
    Performer inviter = performers.byPlatformAndExtId(app.platform(), inviterExtId);
    if (inviter == null)
      return Response.status(404).build();
    Performer performer = performers.byPlatformAndExtId(app.platform(), extId);
    if (performer != null)
      return Response.status(409).build();
    performer = new Performer(extId, app.platform(), inviter);
    performers.put(performer);
    return Response.ok().build();
  }
  
  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long performerId) {
    Performer performer = performers.byId(performerId);
    if (performer == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlPerformer(performer, Details.WITH_RELATED_ENTITIES)).build();
  }
}
