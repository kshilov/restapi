package com.heymoose.infrastructure.service.tracking;

import com.heymoose.domain.base.Repo;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.Response;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.addCookie;

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

    if (!context.getCookieNameValueMap().containsKey(HM_ID_KEY))
      addCookie(response, HM_ID_KEY, "asdf", Integer.MAX_VALUE);
  }
}
