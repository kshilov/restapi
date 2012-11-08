package com.heymoose.infrastructure.service.tracking;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.LeadStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.addCookie;
import static com.heymoose.infrastructure.service.tracking.TrackingUtils.extractReferer;

public final class LeadTracker implements Tracker {

  public static final String HM_ID_KEY = "hm_id";

  private final Repo repo;

  public LeadTracker(Repo repo) {
    this.repo = repo;
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
      Long offerId = Long.valueOf(queryParams.getFirst("offer_id"));
      Offer offer = repo.get(Offer.class, offerId);
      String tokenValue = queryParams.getFirst(offer.tokenParamName());
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
}
