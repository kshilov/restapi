package com.heymoose.infrastructure.service.tracking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.heymoose.domain.request.Request;
import com.heymoose.infrastructure.service.processing.ActionProcessor;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;

public final class ActionTracker implements  Tracker {

  private static final Logger log =
      LoggerFactory.getLogger(ActionTracker.class);

  private final static int MIN_REPEAT_INTERVAL = 5;
  private final static Object DUMMY = new Object();
  private final static Cache<String, Object> RECENT_REQUEST_MAP =
      CacheBuilder.newBuilder()
          .expireAfterAccess(MIN_REPEAT_INTERVAL, TimeUnit.SECONDS)
          .maximumSize(100)
          .build();

  private ActionProcessor actionProcessor;

  @Inject
  public ActionTracker(ActionProcessor processor) {
    this.actionProcessor = processor;
  }

  @Override
  public Response track(HttpRequestContext context) throws ApiRequestException {
    Map<String, String> params = queryParams(context);
    long advertiserId = safeGetLongParam(params, "advertiser_id");
    ensurePresent(params, "transaction_id");
    ensurePresent(params, "offer");
    String sToken = params.get("token");
    if (sToken == null || sToken.length() != 32)
      sToken = context.getCookieNameValueMap()
          .get("hm_token_" + advertiserId).get(0);
    ensureNotNull("token", sToken);

    String requestKey = context.getRequestUri() + ";token=" + sToken;
    if (RECENT_REQUEST_MAP.asMap().putIfAbsent(requestKey, DUMMY) != null) {
      log.warn("Ignoring repeated request: {}", requestKey);
      return Response.status(304).build();
    }
    actionProcessor.process(new Request()
        .setIp(getRealIp(context))
        .addQueryParamsFrom(params)
        .setToken(sToken)
        .setRelativePath(context.getPath()));
    return noCache(Response.ok()).build();
  }
}
