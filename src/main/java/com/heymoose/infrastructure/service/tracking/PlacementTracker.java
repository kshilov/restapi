package com.heymoose.infrastructure.service.tracking;

import com.google.inject.Inject;
import com.heymoose.domain.site.OfferSite;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class PlacementTracker implements Tracker {

  private final Sites sites;

  @Inject
  public PlacementTracker(Sites sites) {
    this.sites = sites;
  }


  @Override
  public void track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {
    MultivaluedMap<String, String> queryParams = context.getQueryParameters();
    String id = queryParams.getFirst("placement_id");
    OfferSite offerSite = sites.getOfferSite(Long.valueOf(id));
    queryParams.putSingle("aff_id", offerSite.site().affiliate().id().toString());
    queryParams.putSingle("offer_id", offerSite.offer().id().toString());
  }
}
