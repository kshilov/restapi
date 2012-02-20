package com.heymoose.resource;

import com.heymoose.domain.OfferShowRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlCount;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("shows")
@Singleton
public class OfferShowResource {
  
  private final OfferShowRepository shows;
  
  @Inject
  public OfferShowResource(OfferShowRepository shows) {
    this.shows = shows;
  }
  
  @GET
  @Path("count")
  @Transactional
  public XmlCount count(@QueryParam("offerId") Long offerId,
      @QueryParam("appId") Long appId,
      @QueryParam("performerId") Long performerId) {
    return Mappers.toXmlCount(shows.count(offerId, appId, performerId));
  }
}
