package com.heymoose.infrastructure.service.tracking;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.counter.BufferedShows;
import com.heymoose.infrastructure.service.OfferStats;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;
import static com.heymoose.resource.api.ApiExceptions.*;

@Singleton
public final class ShowTracker implements Tracker {

  private Repo repo;
  private OfferGrantRepository offerGrants;
  private OfferStats offerStats;
  private BufferedShows bufferedShows;

  @Inject
  public ShowTracker(BufferedShows bufferedShows,
                     OfferGrantRepository offerGrants, OfferStats offerStats,
                     Repo repo) {
    this.bufferedShows = bufferedShows;
    this.offerGrants = offerGrants;
    this.offerStats = offerStats;
    this.repo = repo;
  }

  @Override
  public Response track(HttpRequestContext context) throws ApiRequestException {
    Map<String, String> params = queryParams(context);
    String sBannerId = params.get("banner_id");
    Long bannerId = sBannerId == null ? null : Long.parseLong(sBannerId);
    long offerId = safeGetLongParam(params, "offer_id");
    long affId = safeGetLongParam(params, "aff_id");
    Offer offer = repo.get(Offer.class, offerId);
    if (offer == null)
      throw notFound(Offer.class, offerId);
    User affiliate = repo.get(User.class, affId);
    if (affiliate == null)
      throw notFound(Offer.class, offerId);
    if (offerGrants.visibleByOfferAndAff(offer, affiliate) == null)
      throw illegalState("Offer was not granted: " + offerId);

    Subs subs = new Subs(
        params.get("sub_id"),
        params.get("sub_id1"),
        params.get("sub_id2"),
        params.get("sub_id3"),
        params.get("sub_id4")
    );
    String sourceId = params.get("source_id");

    // tracking
    OfferStat stat = new OfferStat()
        .setMaster(offer.master())
        .setBannerId(bannerId)
        .setOfferId(offerId)
        .setAffiliateId(affId)
        .setSourceId(sourceId)
        .setSubs(subs);
    OfferStat existed = offerStats.findStat(stat);
    if (existed != null) {
      bufferedShows.inc(existed.id());
    } else {
      stat.incShows();
      repo.put(stat);
    }
    return noCache(Response.ok()).build();
  }
}
