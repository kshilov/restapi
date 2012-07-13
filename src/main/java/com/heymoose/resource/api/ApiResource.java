package com.heymoose.resource.api;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimap;
import com.heymoose.domain.model.Banner;
import com.heymoose.infrastructure.GeoTargeting;
import com.heymoose.infrastructure.hibernate.KeywordPatternDao;
import com.heymoose.domain.model.Token;
import com.heymoose.domain.model.User;
import com.heymoose.domain.model.errorinfo.ErrorInfoRepository;
import com.heymoose.domain.model.grant.OfferGrant;
import com.heymoose.domain.model.grant.OfferGrantRepository;
import com.heymoose.domain.model.offer.BaseOffer;
import com.heymoose.domain.model.offer.Offer;
import com.heymoose.domain.model.offer.SubOffer;
import com.heymoose.domain.model.offer.Subs;
import com.heymoose.domain.service.Tracking;
import com.heymoose.domain.model.base.Repo;
import com.heymoose.infrastructure.hibernate.Transactional;
import com.heymoose.util.QueryUtil;
import com.sun.jersey.api.core.HttpRequestContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.google.common.collect.Maps.newHashMap;
import static com.heymoose.resource.api.ApiExceptions.*;
import static com.heymoose.util.QueryUtil.appendQueryParam;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

@Path("api")
@Singleton
public class ApiResource {

  private final static Logger log = LoggerFactory.getLogger(ApiResource.class);

  private final Provider<HttpRequestContext> requestContextProvider;
  private final Provider<UriInfo> uriInfoProvider;
  private final Tracking tracking;
  private final Repo repo;
  private final GeoTargeting geoTargeting;
  private final OfferGrantRepository offerGrants;
  private final KeywordPatternDao keywordPatternDao;
  private final ErrorInfoRepository errorInfoRepo;

  private final static String REQUEST_ID_KEY = "request-id";

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider, Provider<UriInfo> uriInfoProvider,
                     Tracking tracking, Repo repo, GeoTargeting geoTargeting, OfferGrantRepository offerGrants,
                     KeywordPatternDao keywordPatternDao, ErrorInfoRepository errorInfoRepo) {
    this.requestContextProvider = requestContextProvider;
    this.uriInfoProvider = uriInfoProvider;
    this.tracking = tracking;
    this.repo = repo;
    this.geoTargeting = geoTargeting;
    this.offerGrants = offerGrants;
    this.keywordPatternDao = keywordPatternDao;
    this.errorInfoRepo = errorInfoRepo;
  }

  @GET
  public Response callMethod(@QueryParam("method") String method) {
    try {
      String requestId = randomString();
      MDC.put(REQUEST_ID_KEY, requestId);
      return callMethodInternal(method);
    } catch (WebApplicationException e) {
      return errorResponse(e.getMessage(), e, e.getResponse().getStatus(), false);
    } catch (ApiRequestException e) {
      return errorResponse(e.getMessage(), e, e.status, false);
    } catch (Exception e) {
      return errorResponse(e.getMessage(), e, 500, true);
    } finally {
      MDC.remove(REQUEST_ID_KEY);
    }
  }

  private Response callMethodInternal(@QueryParam("method") String method) throws ApiRequestException {

    ensureNotNull("method", method);
    Map<String, String> params = queryParams();
    if (method.equals("track"))
      return track(params);
    else if (method.equals("click"))
      return click(params);
    else if (method.equals("reportAction"))
      return reportAction(params);
    else
      throw badValue("method", method);
  }

  @Transactional
  public Response reportAction(Map<String, String> params) throws ApiRequestException {
    long advertiserId = safeGetLongParam(params, "advertiser_id");
    String txId = safeGetParam(params, "transaction_id");
    String sOffer = safeGetParam(params, "offer");
    String sToken = params.get("token");
    if (sToken == null || sToken.length() != 32)
      sToken = cookies().get("hm_token_" + advertiserId);
    if (sToken == null)
      throw nullParam("token");
    Token token = repo.byHQL(Token.class, "from Token where value = ?", sToken);
    if (token == null)
      throw notFound(Token.class, sToken);
    String[] pairs = sOffer.split(",");
    Map<BaseOffer, Optional<Double>> offers = newHashMap();
    for (String pair : pairs) {
      String[] parts = pair.split(":");
      String code = parts[0];
      BaseOffer offer = findOffer(advertiserId, code);
      if (offer == null)
        throw new ApiRequestException(404, "Offer not found, params code: " + code + ", " + "advertiser_id: " + advertiserId);
      Optional<Double> price = (parts.length == 2)
          ? Optional.of(Double.parseDouble(parts[1]))
          : Optional.<Double>absent();
      offers.put(offer, price);
    }
    tracking.trackConversion(token, txId, offers);
    return noCache(Response.ok()).build();
  }

  private BaseOffer findOffer(long advertiserId, String code) {
    SubOffer existentSub = repo.byHQL(
        SubOffer.class,
        "from SubOffer o where o.active = true and o.code = ? and o.parent.advertiser.id = ?",
        code, advertiserId
    );

    if (existentSub != null)
      return existentSub;

    Offer existentOffer = repo.byHQL(
        Offer.class,
        "from Offer o where o.active = true and o.code = ? and o.advertiser.id = ?",
        code, advertiserId
    );

    return existentOffer;
  }

  @Transactional
  public Response click(Map<String, String> params) throws ApiRequestException {
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
    OfferGrant grant = offerGrants.visibleByOfferAndAff(offer, affiliate);
    if (grant == null)
      return Response.status(409).build();
    if (!visible(offer))
      return forbidden(grant);

    // sourceId and subIds extracting
    Subs subs = new Subs(
        params.get("sub_id"),
        params.get("sub_id1"),
        params.get("sub_id2"),
        params.get("sub_id3"),
        params.get("sub_id4")
    );
    String sourceId = params.get("source_id");

    // geo targeting
    Long ipNum = getRealIp();
    if (ipNum == null)
      throw new ApiRequestException(409, "Can't get IP address");
    if (!geoTargeting.isAllowed(offer, ipNum))
      return forbidden(grant);

    // keywords
    String referer = extractReferer();
    String keywords;
    if (params.containsKey("keywords"))
      keywords = params.get("keywords");
    else
      keywords = keywordPatternDao.extractKeywords(referer);

    // postback feature parameters
    Map<String, String> affParams = newHashMap(params);
    for (String param : asList("method", "banner_id", "offer_id", "aff_id",
        "sub_id", "sub_id1", "sub_id2", "sub_id3", "sub_id4", "source_id"))
      affParams.remove(param);

    // track
    String token = tracking.trackClick(bannerId, offerId, offer.master(), affId, sourceId, subs, affParams, referer, keywords);

    // location
    URI location = null;
    if (offer.allowDeeplink()) {
      String ulp = params.get("ulp");
      if (ulp != null) {
        try {
          location = QueryUtil.removeQueryParam(URI.create(ulp), "ulp");
        } catch (IllegalArgumentException e) {
          location = null;
        }
      }
    }
    if (location == null) {
      Banner banner = (bannerId == null) ? null : repo.get(Banner.class, bannerId);
      location = (banner != null && banner.url() != null) ? URI.create(banner.url()) : URI.create(offer.url());
    }
    location = appendQueryParam(location, offer.tokenParamName(), token);
    location = appendQueryParam(location, "_hm_ttl", offer.cookieTtl());
    Response.ResponseBuilder response = Response.status(302).location(location);
    int maxAge = Seconds.secondsBetween(DateTime.now(), DateTime.now().plusDays(offer.cookieTtl())).getSeconds();
    addCookie(response, "hm_token_" + offer.advertiser().id(), token, maxAge);
    noCache(response);
    return response.build();
  }

  private String extractReferer() {
    if (requestContextProvider.get().getHeaderValue("Referer") != null)
      return requestContextProvider.get().getHeaderValue("Referer");
    else
      return requestContextProvider.get().getHeaderValue("X-Real-IP");
  }

  private static boolean visible(BaseOffer offer) {
    if (offer instanceof Offer) {
      Offer newOffer = (Offer) offer;
      return newOffer.visible();
    } else if (offer instanceof SubOffer) {
      SubOffer subOffer = (SubOffer) offer;
      return subOffer.active();
    } else {
      return false;
    }
  }

  @Transactional
  public Response track(Map<String, String> params) throws ApiRequestException {
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
    tracking.trackShow(bannerId, offerId, offer.master(), affId, sourceId, subs);
    return noCache(Response.ok()).build();
  }

  private Map<String, String> queryParams() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getQueryParameters().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
  }

  private Map<String, String> cookies() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getCookieNameValueMap().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
  }

  private Long getRealIp() {
    String hRealIp = requestContextProvider.get().getHeaderValue("X-Real-IP");
    if (hRealIp == null)
      return null;
    String[] parts = hRealIp.split("\\.");
    long a = Long.valueOf(parts[0]);
    long b = Long.valueOf(parts[1]);
    long c = Long.valueOf(parts[2]);
    long d = Long.valueOf(parts[3]);
    return (a << 24) | (b << 16) | (c << 8) | d;
  }

  private Multimap<String, String> queryParamsMulti() {
    Multimap<String, String> params = HashMultimap.create();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getQueryParameters().entrySet())
      params.putAll(ent.getKey(), ent.getValue());
    return params;
  }

  public long safeGetLongParam(Map<String, String> params, String paramName) throws ApiRequestException {
    String val = safeGetParam(params, paramName);
    try {
      return Long.valueOf(val);
    } catch (NumberFormatException e) {
      throw badValue(paramName, val);
    }
  }

  public int safeGetIntParam(Map<String, String> params, String paramName) throws ApiRequestException {
    String val = safeGetParam(params, paramName);
    try {
      return Integer.valueOf(val);
    } catch (NumberFormatException e) {
      throw badValue(paramName, val);
    }
  }

  private Integer parseInt(String name, String s) throws ApiRequestException {
    try {
      return Integer.valueOf(s);
    } catch (NumberFormatException e) {
      throw badValue(name, s);
    }
  }

  private Long parseLong(String name, String s) throws ApiRequestException {
    try {
      return Long.valueOf(s);
    } catch (NumberFormatException e) {
      throw badValue(name, s);
    }
  }


  public String safeGetParam(Map<String, String> params, String paramName) throws ApiRequestException {
    String val = params.get(paramName);
    ensureNotNull(paramName, val);
    return val;
  }

  private static void ensureNotNull(String paramName, Object value) throws ApiRequestException {
    if (value == null)
      throw nullParam(paramName);
  }

  private static Response successResponse() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsSuccess = mapper.createObjectNode();
    jsSuccess.put("success", true);
    String json;
    try {
      json = mapper.writeValueAsString(jsSuccess);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return Response.ok(json).build();
  }


  private Response errorResponse(String message, Throwable cause, int status, boolean includeStackTrace) {
    URI requestUri = uriInfoProvider.get().getRequestUri();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsError = mapper.createObjectNode();
    jsError.put("success", false);
    jsError.put("status", status);
    jsError.put("error", message);
    jsError.put("query", requestUri.getQuery());
    if (includeStackTrace) {
      jsError.put("details", fetchStackTrace(cause));
    }
    jsError.put("requestId", MDC.get(REQUEST_ID_KEY));
    String json;
    try {
      json = mapper.writeValueAsString(jsError);
    } catch (IOException e) {
      throw new RuntimeException("JSON error", e);
    }
    log.error("Error while processing request: " + requestUri, cause);
    storeError(requestUri, cause);
    throw new WebApplicationException(cause, Response.status(status).entity(json).build());
  }

  /**
   * Method is not `private`, because {@link Transactional} interceptor does not
   * work in this case.
   *
   * @param uri
   * @param cause
   */
  @Transactional
  protected void storeError(URI uri, Throwable cause) {
    Map<String, String> params = queryParams();

    // Build uri part with query string params sorted
    StringBuilder uriBuilder = new StringBuilder();
    ImmutableSortedMap<String, String> sortedParams =
        ImmutableSortedMap.copyOf(params);
    uriBuilder.append(uri.getPath());
    uriBuilder.append('?');
    for (Map.Entry<String, String> param : sortedParams.entrySet()) {
      uriBuilder.append(param.getKey());
      uriBuilder.append('=');
      uriBuilder.append(param.getValue());
      uriBuilder.append('&');
    }
    uriBuilder.setLength(uriBuilder.length() - 1); // remove redundant symbol
    errorInfoRepo.track(uriBuilder.toString(), DateTime.now(), cause);
  }

  private static String fetchStackTrace(Throwable th) {
    Writer writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    th.printStackTrace(pw);
    return writer.toString();
  }

  private static String randomString() {
    return randomAlphanumeric(10);
  }

  private static Response forbidden(OfferGrant grant) {
    if (grant.backUrl() == null)
      return Response.status(403).build();
    else
      return Response.status(302).location(URI.create(grant.backUrl())).build();
  }

  private static void addCookie(Response.ResponseBuilder resp, String name, String value, int age) {
    long expires = System.currentTimeMillis() + age * 1000L;
    resp.header("Set-Cookie", String.format("%s=%s;Version=1;expires=%s;Path=/", name, value, formatAsGMT(expires)));
  }

  private static String formatAsGMT(long time) {
    // Wed, 19 Jan 2011 12:05:26 GMT
    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    return format.format(new Date(time));
  }

  private static Response.ResponseBuilder noCache(Response.ResponseBuilder response) {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge(0);
    cacheControl.setNoCache(true);
    return response.cacheControl(cacheControl);
  }
}
