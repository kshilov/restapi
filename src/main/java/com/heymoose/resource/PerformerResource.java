package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.PerformerRepository;
import com.heymoose.hibernate.Transactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
}
