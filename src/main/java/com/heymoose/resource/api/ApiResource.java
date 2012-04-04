package com.heymoose.resource.api;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Multimap;
import com.heymoose.domain.BaseOffer;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.affiliate.ClickStat;
import com.heymoose.domain.affiliate.GeoTargeting;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.affiliate.Tracking;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.api.ApiExceptions.badValue;
import static com.heymoose.resource.api.ApiExceptions.illegalState;
import static com.heymoose.resource.api.ApiExceptions.notFound;
import static com.heymoose.resource.api.ApiExceptions.nullParam;
import com.sun.jersey.api.core.HttpRequestContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Path("api")
@Singleton
public class ApiResource {

  private final static Logger log = LoggerFactory.getLogger(ApiResource.class);

  private final Provider<HttpRequestContext> requestContextProvider;
  private final Provider<UriInfo> uriInfoProvider;
  private final Tracking tracking;
  private final Repo repo;
  private final GeoTargeting geoTargeting;
  
  private final static String REQUEST_ID_KEY = "request-id";

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider,
                     Provider<UriInfo> uriInfoProvider, Tracking tracking, Repo repo,
                     GeoTargeting geoTargeting) {
    this.requestContextProvider = requestContextProvider;
    this.uriInfoProvider = uriInfoProvider;
    this.tracking = tracking;
    this.repo = repo;
    this.geoTargeting = geoTargeting;
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
    Long clickId = safeGetLongParam(params, "click_id");
    String txId = safeGetParam(params, "transaction_id");
    String sOffer = safeGetParam(params, "offer");
    String[] pairs = sOffer.split(",");
    Map<BaseOffer, Optional<Double>> offers = newHashMap();
    for (String pair : pairs) {
      String[] parts = pair.split(":");
      String sOfferId = parts[0];
      long offerId = Long.valueOf(sOfferId);
      BaseOffer offer = repo.get(BaseOffer.class, offerId);
      Optional<Double> price = (parts.length == 2)
          ? Optional.of(Double.parseDouble(parts[1]))
          : Optional.<Double>absent();
      offers.put(offer, price);
    }
    ClickStat click = repo.get(ClickStat.class, clickId);
    tracking.actionDone(click, txId, offers);
    return Response.ok().build();
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
    OfferGrant grant = tracking.granted(offer, affiliate);
    if (grant == null)
      return Response.status(409).build();
    if (!visible(offer))
      return forbidden(grant);
    String subId = params.get("sub_id");
    String sourceId = params.get("source_id");
    Long ipNum = getRealIp();
    if (ipNum == null)
      throw new ApiRequestException(409, "Can't get IP address");
    if (!geoTargeting.isAllowed(offer, ipNum))
      return forbidden(grant);
    ClickStat click = tracking.click(bannerId, offerId, affId, subId, sourceId);
    URI location = URI.create(offer.url());
    location = Api.appendQueryParam(location, "_hm_click_id", click.id());
    return Response.status(302).location(location).build();
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
    if (tracking.granted(offer, affiliate) == null)
      throw illegalState("Offer was not granted: " + offerId);
    String subId = params.get("sub_id");
    String sourceId = params.get("source_id");
    tracking.track(bannerId, offer, affiliate, subId, sourceId);
    return Response.ok().build();
  }

  private Map<String, String> queryParams() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getQueryParameters().entrySet())
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
      throw new RuntimeException(e.getMessage(), e);
    }
    log.error("Error while processing request: " + requestUri, cause);
    throw new WebApplicationException(cause, Response.status(status).entity(json).build());
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
}
