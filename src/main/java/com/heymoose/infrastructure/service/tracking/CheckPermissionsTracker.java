package com.heymoose.infrastructure.service.tracking;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.Placement;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;
import static com.heymoose.resource.api.ApiExceptions.notFound;

public final class CheckPermissionsTracker implements Tracker {

  private static final Logger log =
      LoggerFactory.getLogger(CheckPermissionsTracker.class);

  private final Repo repo;
  private final Sites sites;

  @Inject
  public CheckPermissionsTracker(Repo repo, Sites sites) {
    this.repo = repo;
    this.sites = sites;
  }

  @Override
  public boolean track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {

    Map<String, String> params = queryParams(context);
    log.debug("Entering permissions check. Query params: {}", params);
    long affId = safeGetLongParam(params, "aff_id");
    long offerId = safeGetLongParam(params, "offer_id");

    User affiliate = repo.get(User.class, affId);
    if (affiliate == null) {
      log.debug("Affiliate with id {} not found. Throwing not found.", affId);
      throw notFound(User.class, affId);
    }


    Offer offer = repo.get(Offer.class, offerId);
    if (offer == null) {
      log.debug("Offer with id {} not found. Throwing not found.", offerId);
      throw notFound(Offer.class, offerId);
    }

    String backUrl = null;
    Site site = null;
    if (params.containsKey("site_id")) {
      Long siteId = Long.valueOf(params.get("site_id"));
      site = sites.get(siteId);
    } else {
      // default site
      site = sites.findSite(affId, Site.Type.GRANT);
      if (site == null) {
        log.warn("No default site for aff {}", affId);
        response.status(404);
        return false;
      }
      context.getQueryParameters().putSingle("site_id", site.id().toString());
    }
    try {
      Placement placement = sites.checkPermission(offer, site);
      String referer = context.getHeaderValue("Referer");
      if(!site.matches(referer)) {
        log.debug("Referer {} does not match site {}. Forbidden.",
            referer, site);
        forbidden(placement.backUrl(), response);
        return false;
      }
      backUrl = placement.backUrl();
    } catch (IllegalStateException e) {
      log.error("Returning 409", e);
      response.status(409);
      return false;
    }

    if (!affiliate.confirmed() || !affiliate.active()) {
      log.debug("Affiliate forbidden {}", affiliate);
      forbidden(backUrl, response);
      return false;
    }

    context.getQueryParameters().putSingle("back_url", backUrl);
    return true;
  }
}
