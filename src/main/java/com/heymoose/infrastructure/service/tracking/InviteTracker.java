package com.heymoose.infrastructure.service.tracking;

import com.google.common.base.Strings;
import com.heymoose.resource.api.ApiExceptions;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.addCookie;
import static com.heymoose.infrastructure.service.tracking.TrackingUtils.queryParams;

public class InviteTracker implements Tracker {

  public static final String COOKIE_NAME = "hm_invite";
  public static final String REFERRER_PARAM = "referer";
  public static final String LOCATION_PARAM = "ulp";

  public void track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {
    Map<String, String> queryParams = queryParams(context);
    String referer = queryParams.get(REFERRER_PARAM);
    String ulp = queryParams.get(LOCATION_PARAM);

    if (Strings.isNullOrEmpty(referer))
      throw ApiExceptions.nullParam(REFERRER_PARAM);
    if (Strings.isNullOrEmpty(ulp))
      throw ApiExceptions.nullParam(LOCATION_PARAM);

    addCookie(response, COOKIE_NAME, referer, Integer.MAX_VALUE);
    response.status(302).header("Location", ulp);
  }
}
