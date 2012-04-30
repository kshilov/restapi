package com.heymoose.resource;

import com.heymoose.domain.Mlm;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;

@Path("mlm")
@Singleton
public class MlmResource {

  private final Mlm mlm;

  @Inject
  public MlmResource(Mlm mlm) {
    this.mlm = mlm;
  }

  @GET
  public Response run(@QueryParam("start") Long start, @QueryParam("stop") Long stop) {
    if (start == null)
      return Response.status(400).build();
    mlm.doMlmExport(new DateTime(start), new DateTime(stop));
    return Response.ok().build();
  }
}
