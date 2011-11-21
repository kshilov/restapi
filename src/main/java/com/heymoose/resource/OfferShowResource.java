package com.heymoose.resource;

import com.heymoose.domain.OfferShowRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("shows")
@Singleton
public class OfferShowResource {
  
  private final OfferShowRepository shows;
  
  @Inject
  public OfferShowResource(OfferShowRepository shows) {
    this.shows = shows;
  }
  
  @GET
  @Transactional
  public Response list(@QueryParam("from") Long from,
                       @QueryParam("to") Long to,
                       @QueryParam("offerId") Long offerId,
                       @QueryParam("appId") Long appId,
                       @QueryParam("performerId") Long performerId) {
    checkNotNull(from, to);
    DateTime dtFrom = new DateTime(from * 1000);
    DateTime dtTo = new DateTime(to * 1000);
    System.out.println(dtFrom.toString());
    System.out.println(dtTo.toString());
    return Response.ok(Mappers.toXmlOfferShows(
        shows.list(dtFrom, dtTo, offerId, appId, performerId))
    ).build();
  }
}
