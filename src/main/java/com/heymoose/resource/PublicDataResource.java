package com.heymoose.resource;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.infrastructure.service.PublicData;
import com.heymoose.infrastructure.util.Cacheable;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.resource.xml.XmlQueryResult;
import org.joda.time.DateTime;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Singleton
@Path("public")
public class PublicDataResource {

  private final PublicData publicData;

  @Inject
  public PublicDataResource(PublicData publicData) {
    this.publicData = publicData;
  }


  private static Response ok(String responseBody) {
    return Response.ok(responseBody)
        .expires(DateTime.now().plusHours(12).toDate())
        .build();
  }

  @GET
  @Path("affiliate/top-withdraw")
  @Produces("application/xml")
  @Cacheable
  public Response topWithdrawAffiliates(
      @QueryParam("limit") @DefaultValue("5") int limit) {
    QueryResult data = publicData.topWithdrawAffiliates(limit);
    return ok(new XmlQueryResult(data)
        .setRoot("result")
        .setElement("affiliate")
        .toString());
  }

  @GET
  @Path("affiliate/top-conversion")
  @Produces("application/xml")
  @Cacheable
  public Response topConversionAffiliates(
      @QueryParam("limit") @DefaultValue("5") int limit) {
    QueryResult result = publicData.topConversionAffiliates(limit);
    return ok(new XmlQueryResult(result)
        .setRoot("result")
        .setElement("affiliate")
        .toString());
  }

  @GET
  @Path("offer/count")
  @Cacheable
  public Response activeOfferCount() {
    Long result = publicData.countActiveOffers();
    return ok(result.toString());
  }


}
