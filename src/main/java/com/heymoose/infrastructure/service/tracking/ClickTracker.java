package com.heymoose.infrastructure.service.tracking;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Banner;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.Subs;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.counter.BufferedClicks;
import com.heymoose.infrastructure.persistence.KeywordPatternDao;
import com.heymoose.infrastructure.service.BlackList;
import com.heymoose.infrastructure.service.GeoTargeting;
import com.heymoose.infrastructure.service.OfferStats;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.QueryUtil;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;
import static com.heymoose.infrastructure.util.QueryUtil.appendQueryParam;

@Singleton
public class ClickTracker implements Tracker {

  private static final Logger log = LoggerFactory.getLogger(ClickTracker.class);

  private final Repo repo;
  private final GeoTargeting geoTargeting;
  private final OfferStats offerStats;
  private final KeywordPatternDao keywordPatternDao;
  private final BufferedClicks bufferedClicks;
  private final BlackList blackList;
  private final Sites sites;

  @Inject
  public ClickTracker(BufferedClicks bufferedClicks, GeoTargeting geoTargeting,
                      KeywordPatternDao keywordPatternDao,
                      OfferStats offerStats,
                      BlackList blackList,
                      Sites sites,
                      Repo repo) {
    this.bufferedClicks = bufferedClicks;
    this.geoTargeting = geoTargeting;
    this.keywordPatternDao = keywordPatternDao;
    this.offerStats = offerStats;
    this.blackList = blackList;
    this.repo = repo;
    this.sites = sites;
  }

  @Override
  public boolean track(HttpRequestContext context,
                    Response.ResponseBuilder response)
      throws ApiRequestException {
    Map<String, String> params = queryParams(context);
    log.debug("Entering click tracking. Query params: {}", params);
    String sBannerId = params.get("banner_id");
    Long bannerId = sBannerId == null ? null : Long.parseLong(sBannerId);
    long offerId = safeGetLongParam(params, "offer_id");
    long affId = safeGetLongParam(params, "aff_id");
    Offer offer = repo.get(Offer.class, offerId);

    // sourceId and subIds extracting
    Subs subs = new Subs(
        params.get("sub_id"),
        params.get("sub_id1"),
        params.get("sub_id2"),
        params.get("sub_id3"),
        params.get("sub_id4")
    );
    String sourceId = params.get("source_id");

    String backUrl = params.get("back_url");

    // geo targeting
    Long ipNum = getRealIp(context);
    if (ipNum == null)
      throw new ApiRequestException(409, "Can't get IP address");
    if (!geoTargeting.isAllowed(offer, ipNum)) {
      forbidden(backUrl, response);
      return false;
    }

    if (blackList.ban(context.getHeaderValue("Referer"))) {
      forbidden(backUrl, response);
      return false;
    }
    String referer = extractReferer(context);

    // keywords
    String keywords;
    if (params.containsKey("keywords"))
      keywords = params.get("keywords");
    else
      keywords = keywordPatternDao.extractKeywords(referer);

    // postback feature parameters
    Map<String, String> affParams = Maps.newHashMap(params);
    for (String param : ImmutableList.of("method", "banner_id", "offer_id",
        "aff_id", "sub_id", "sub_id1", "sub_id2", "sub_id3", "sub_id4",
        "source_id"))
      affParams.remove(param);

    String cashbackReferrer = context
            .getCookieNameValueMap()
            .getFirst(InviteTracker.COOKIE_NAME + "_" + affId);

    // track
    OfferStat stat = new OfferStat()
        .setBannerId(bannerId)
        .setOfferId(offerId)
        .setMaster(offer.master())
        .setAffiliateId(affId)
        .setSourceId(sourceId)
        .setSubs(subs)
        .setReferer(referer)
        .setKeywords(keywords)
        .setCashbackTargetId(params.get("cashback"))
        .setCashbackReferrer(cashbackReferrer)
        .setSiteId(Long.valueOf(params.get("site_id")));
    OfferStat existedStat = offerStats.findStat(stat);
    if (existedStat == null) {
      stat.incClicks();
      repo.put(stat);
    } else {
      bufferedClicks.inc(existedStat.id());
      stat = existedStat;
    }
    Token token = new Token(stat);
    token.setAffParams(affParams);
    repo.put(token);

    // location
    URI location = null;
    if (offer.allowDeeplink()) {
      String ulp = params.get("ulp");
      if (ulp != null) {
        if (!ulp.contains("://")) {
          ulp = "http://" + ulp;
        }
        try {
          location = QueryUtil.removeQueryParam(URI.create(ulp), "ulp");
        } catch (IllegalArgumentException e) {
          location = null;
        }
      }
    }
    if (location == null) {
      Banner banner = (bannerId == null) ? null : repo
          .get(Banner.class, bannerId);
      location = (banner != null && banner.url() != null) ? URI
          .create(banner.url()) : URI
          .create(offer.url());
    }
    location = appendQueryParam(location, offer.tokenParamName(),
        token.value());
    location = appendQueryParam(location, "_hm_ttl", offer.cookieTtl());

    String getParams = offer.requiredGetParameters();
    if (!Strings.isNullOrEmpty(getParams)) {
      for (String keyVal : getParams.split("&")) {
        String[] kv = keyVal.split("=");
        location = QueryUtil.appendQueryParam(location, kv[0], kv[1]);
      }
    }

    response.status(302).location(location);
    int maxAge = Seconds.secondsBetween(DateTime.now(),
        DateTime.now().plusDays(offer.cookieTtl())).getSeconds();
    addCookie(response, "hm_token_" + offer.advertiser().id(), token.value(),
        maxAge);
    noCache(response);
    return true;
  }

}
