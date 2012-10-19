package com.heymoose.infrastructure.service.tracking;

import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.processing.ActionProcessor;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;

public final class ActionTracker implements  Tracker {

  private static final Logger log =
      LoggerFactory.getLogger(ActionTracker.class);

  private final static Splitter PERIOD_SPLITTER = Splitter.on(',');
  private final static Splitter COLON_SPLITTER = Splitter.on(':');

  private final static int MIN_REPEAT_INTERVAL = 5;
  private final static Object DUMMY = new Object();
  private final static Cache<String, Object> RECENT_REQUEST_MAP =
      CacheBuilder.newBuilder()
          .expireAfterAccess(MIN_REPEAT_INTERVAL, TimeUnit.SECONDS)
          .maximumSize(100)
          .build();

  private ActionProcessor actionProcessor;
  private OfferLoader offerLoader;
  private Repo repo;

  @Inject
  public ActionTracker(Repo repo, ActionProcessor processor,
                       OfferLoader offerLoader) {
    this.actionProcessor = processor;
    this.offerLoader = offerLoader;
    this.repo = repo;
  }

  @Override
  public Response track(HttpRequestContext context) throws ApiRequestException {
    Map<String, String> params = queryParams(context);
    long advertiserId = safeGetLongParam(params, "advertiser_id");
    String offerString = safeGetParam(params, "offer");
    String transactionId = safeGetParam(params, "transaction_id");
    ensurePresent(params, "offer");
    String sToken = params.get("token");
    if (sToken == null || sToken.length() != 32)
      sToken = context.getCookieNameValueMap()
          .getFirst("hm_token_" + advertiserId);
    ensureNotNull("token", sToken);
    Token token = checkToken(repo, sToken);

    String requestKey = context.getRequestUri() + ";token=" + sToken;
    if (RECENT_REQUEST_MAP.asMap().putIfAbsent(requestKey, DUMMY) != null) {
      log.warn("Ignoring repeated request: {}", requestKey);
      return Response.status(304).build();
    }

    if (!offerString.contains(":")) {
      BaseOffer offer = offerLoader.findOffer(advertiserId, offerString);
      ProcessableData data = new ProcessableData()
          .setToken(token)
          .setTransactionId(transactionId)
          .setOffer(offer);
      actionProcessor.process(data);
    } else {
      for (String keyVal : PERIOD_SPLITTER.split(offerString)) {
        Iterator<String> keyValIterator = COLON_SPLITTER.split(keyVal)
            .iterator();
        BaseOffer offer = offerLoader.findOffer(
            advertiserId, keyValIterator.next());
        ProcessableData data = new ProcessableData()
            .setToken(token)
            .setTransactionId(transactionId)
            .setOffer(offer)
            .setPrice(new BigDecimal(keyValIterator.next()));
        actionProcessor.process(data);
      }
    }
    return noCache(Response.ok()).build();
  }
}