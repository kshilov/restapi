package com.heymoose.infrastructure.service.tracking;

import com.google.common.base.Strings;
import com.heymoose.resource.api.ApiExceptions;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;

public class InviteTracker implements Tracker {

  private static final Logger log =
      LoggerFactory.getLogger(InviteTracker.class);

  public static final String COOKIE_NAME = "hm_cashback_invite";
  public static final String AFF_ID_PARAM = "aff_id";
  public static final String REFERRER_PARAM = "referrer";
  public static final String LOCATION_PARAM = "ulp";

  public void track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {
    Map<String, String> queryParams = queryParams(context);
    String referrer = queryParams.get(REFERRER_PARAM);
    String ulp = queryParams.get(LOCATION_PARAM);
    String affId = queryParams.get("aff_id");

    if (Strings.isNullOrEmpty(referrer))
      throw ApiExceptions.nullParam(REFERRER_PARAM);
    if (Strings.isNullOrEmpty(ulp))
      throw ApiExceptions.nullParam(LOCATION_PARAM);
    if (Strings.isNullOrEmpty(affId)) {
      throw ApiExceptions.nullParam(AFF_ID_PARAM);
    }

    try {
      Long.parseLong(affId);
    } catch (NumberFormatException e) {
      throw ApiExceptions.badValue(AFF_ID_PARAM, affId);
    }

    addCookie(response, COOKIE_NAME + "_" + affId, referrer, Integer.MAX_VALUE);
    if (!ulp.contains("://")) ulp = "http://" + ulp;
    log.info("Tracking cashback invite. aff_id:{}, referrer: {}, ulp: {}",
        new Object[] { affId, referrer, ulp });
    response.status(302).header("Location", ulp);
  }
}
