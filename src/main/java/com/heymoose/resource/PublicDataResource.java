package com.heymoose.resource;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.infrastructure.service.PublicData;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.resource.xml.XmlQueryResult;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Singleton
@Path("public")
public final class PublicDataResource {

  private final PublicData publicData;

  @Inject
  public PublicDataResource(PublicData publicData) {
    this.publicData = publicData;
  }

  @GET
  @Path("affiliate/top-withdraw")
  @Produces("application/xml")
  public String topWithdrawAffiliates(
      @QueryParam("limit") @DefaultValue("5") int limit) {
    QueryResult data = publicData.topWithdrawAffiliates(limit);
    return new XmlQueryResult(data)
        .setRoot("result")
        .setElement("affiliate")
        .toString();
  }

}
