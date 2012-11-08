package com.heymoose.infrastructure.service.tracking;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.LeadStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Objects.firstNonNull;
import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;

public final class LeadTracker implements Tracker {

  public static final String HM_ID_KEY = "hm_id";

  private static final Logger log = LoggerFactory.getLogger(LeadTracker.class);
  private static final Random RANDOM = new Random();

  private final Repo repo;
  private final OfferLoader offerLoader;

  @Inject
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
      leadKey = new BigInteger(160, RANDOM).toString(32);
      addCookie(response, HM_ID_KEY, leadKey, Integer.MAX_VALUE);
    }

    try {
      MultivaluedMap<String, String> queryParams = context.getQueryParameters();
      Offer offer = getOffer(queryParams);
      String cookieName = "hm_token_" + offer.advertiser().id();
      String tokenValueRequestCookie = cookies.getFirst(cookieName);
      String tokenValueResponseCookie = extractCookie(response, cookieName);
      if (tokenValueRequestCookie == null && tokenValueResponseCookie == null) {
        log.info("Can't get token cookie. Skipping lead tracking. {}", offer);
        return;
      }
      String tokenValue = firstNonNull(
          tokenValueRequestCookie,
          tokenValueResponseCookie);
      Token token = repo.byHQL(Token.class,
          "from Token where value = ?", tokenValue);

      LeadStat leadStat = new LeadStat()
          .setKey(leadKey)
          .setToken(token)
          .setIp(context.getHeaderValue("X-Real-IP"))
          .setReferrer(extractReferer(context))
          .setMethod(queryParams.getFirst("method"));
      repo.put(leadStat);
    } catch (Exception e) {
      log.error("Exception during tracking lead: {}", e);
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
      code = code.split(":")[0];
      return offerLoader.findOffer(advId, code).masterOffer();
    }
    throw new IllegalArgumentException(
        "Missing parameters, required for finding offer.");
  }

  private String extractCookie(Response.ResponseBuilder response, String key) {
    List<Object> cookies = response
        .clone()
        .build()
        .getMetadata()
        .get("Set-Cookie");
    if (cookies == null) return null;
    for (Object cookieString : cookies) {
      Cookie cookie = Cookie.valueOf(cookieString.toString());
      if (cookie.getName().equals(key)) return cookie.getValue();
    }
    return null;
  }
}
