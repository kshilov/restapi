package com.heymoose.infrastructure.service.tracking;

import com.google.inject.Inject;
import com.heymoose.domain.site.Placement;
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
  public boolean track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {
    MultivaluedMap<String, String> queryParams = context.getQueryParameters();
    String id = queryParams.getFirst("placement_id");
    Placement placement = sites.getOfferSite(Long.valueOf(id));
    queryParams.putSingle("aff_id", placement.site().affiliate().id().toString());
    queryParams.putSingle("offer_id", placement.offer().id().toString());
    queryParams.putSingle("site_id", placement.site().id().toString());
    return true;
  }
}
