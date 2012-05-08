package com.heymoose.resource;

import com.heymoose.domain.mlm.Mlm;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("mlm")
@Singleton
public class MlmResource {

  private final Mlm mlm;

  @Inject
  public MlmResource(Mlm mlm) {
    this.mlm = mlm;
  }

  @GET
  public Response run() {
    mlm.doExport();
    return Response.ok().build();
  }
}
