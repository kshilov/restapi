package com.heymoose.infrastructure.service.tracking;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.LeadStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static com.google.common.base.Objects.firstNonNull;
import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;

public final class LeadTracker implements Tracker {

  public static final String HM_ID_KEY = "hm_id";

  private final Repo repo;
  private OfferLoader offerLoader;

  public LeadTracker(Repo repo) {
    this.repo = repo;
  }

  public LeadTracker(Repo repo, OfferLoader loader) {
    this.repo = repo;
    this.offerLoader = loader;
  }

  @Override
  public void track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {

    MultivaluedMap<String, String> cookies = context.getCookieNameValueMap();
    String leadKey = cookies.getFirst(HM_ID_KEY);
    if (leadKey == null) {
      leadKey = "leadKey";
      addCookie(response, HM_ID_KEY, leadKey, Integer.MAX_VALUE);
    }

    try {
      MultivaluedMap<String, String> queryParams = context.getQueryParameters();
      Offer offer = getOffer(queryParams);
      String tokenValueQuery = queryParams.getFirst(offer.tokenParamName());
      String tokenValueCookie = cookies.getFirst(
          "hm_token_" + offer.advertiser().id());
      String tokenValue = firstNonNull(tokenValueQuery, tokenValueCookie);
      Token token = repo.byHQL(Token.class,
          "from Token where value = ?", tokenValue);

      LeadStat leadStat = new LeadStat()
          .setKey(leadKey)
          .setToken(token)
          .setIp(context.getHeaderValue("X-Real-IP"))
          .setReferrer(extractReferer(context));
      repo.put(leadStat);
    } catch (Exception e) {
      // ignore for now
    }
  }

  private Offer getOffer(MultivaluedMap<String, String> queryParams) {
    if (queryParams.containsKey("offer_id")) {
      Long offerId = Long.valueOf(queryParams.getFirst("offer_id"));
      return offerLoader.offerById(offerId);
    }
    if (queryParams.containsKey("offer") &&
        queryParams.containsKey("advertiser_id")) {
      String code = queryParams.getFirst("offer");
      Long advId = Long.valueOf(queryParams.getFirst("advertiser_id"));
      return offerLoader.findOffer(advId, code).masterOffer();
    }
    throw new IllegalArgumentException(
        "Missing parameters, required for finding offer.");
  }
}
