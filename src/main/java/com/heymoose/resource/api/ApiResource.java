package com.heymoose.resource.api;

import com.google.common.collect.HashMultimap;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Multimap;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Performer;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.JsonOfferTemplate;
import com.heymoose.resource.OfferTemplate;
import static com.heymoose.resource.api.ApiExceptions.appNotFound;
import static com.heymoose.resource.api.ApiExceptions.badSignature;
import static com.heymoose.resource.api.ApiExceptions.badValue;
import static com.heymoose.resource.api.ApiExceptions.customerNotFound;
import static com.heymoose.resource.api.ApiExceptions.notInRole;
import static com.heymoose.resource.api.ApiExceptions.nullParam;
import com.heymoose.resource.api.data.OfferData;
import static com.heymoose.security.Signer.sign;
import com.sun.jersey.api.core.HttpRequestContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
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
  private final AppRepository apps;
  private final UserRepository users;
  private final OfferTemplate jsonTemplate = new JsonOfferTemplate();
  private final Api api;
  private final Provider<UriInfo> uriInfoProvider;
  
  private final static String REQUEST_ID_KEY = "request-id";

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider, AppRepository apps, Api api,
                     UserRepository users, Provider<UriInfo> uriInfoProvider) {
    this.requestContextProvider = requestContextProvider;
    this.apps = apps;
    this.api = api;
    this.users = users;
    this.uriInfoProvider = uriInfoProvider;
  }

  @GET
  public Response callMethod(@QueryParam("method") String method,
                             @QueryParam("sig") String sig,
                             @QueryParam("format") @DefaultValue("HTML") String format) {
    try {
      String requestId = randomString();
      MDC.put(REQUEST_ID_KEY, requestId);
      return callMethodInternal(method, sig, format);
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

  private Response callMethodInternal(@QueryParam("method") String method,
                             @QueryParam("sig") String sig,
                             @QueryParam("format") @DefaultValue("HTML") String format) throws ApiRequestException {
    ensureNotNull("method", method);
    ensureNotNull("sig", sig);
    if (!asList("HTML", "JSON").contains(format))
      throw badValue("format", format);
    Map<String, String> params = queryParams();
    if (method.equals("getOffers"))
      return getOffers(format, params);
    else if (method.equals("doOffer"))
      return doOffer(params);
    else if (method.equals("approveAction"))
      return approveAction(params);
    else if (method.equals("introducePerformer"))
      return introducePerformer(params);
    else if (method.equals("reportShow"))
      return reportShow(params, queryParamsMulti());
    else
      throw badValue("method", method);
  }

  private Response reportShow(Map<String, String> params, Multimap<String, String> multiParams) throws ApiRequestException {
    long appId = safeGetLongParam(params, "app_id");
    App app = validateAppSig(appId, params);
    String extId = safeGetParam(params, "uid");
    List<Long> offers = newArrayList();
    for (String _offerId : multiParams.get("offer_id"))
      offers.add(parseLong("offer_id", _offerId));
    api.reportShow(offers, app, extId);
    return successResponse();
  }

  private Response introducePerformer(Map<String, String> params) throws ApiRequestException {
    long appId = safeGetLongParam(params, "app_id");
    App app = validateAppSig(appId, params);
    String extId = safeGetParam(params, "uid");
    String _sex = params.get("sex");
    if (_sex != null && !asList("MALE", "FEMALE").contains(_sex))
      throw badValue("sex", _sex);
    Boolean male = (_sex == null) ? null : "MALE".equals(_sex);
    String _year = params.get("year");
    Integer year = (_year == null) ? null : parseInt("year", _year);
    String city = params.get("city");
    api.introducePerformer(app, extId, new Performer.Info(male, year, city));
    return successResponse();
  }

  private final static Pattern RE_FILTER = Pattern.compile("^\\d+:\\d+(:\\d+x\\d+)?(,\\d+:\\d+(:\\d+x\\d+)?)*$");

  private Response getOffers(String format, Map<String, String> params) throws ApiRequestException {
    long appId = safeGetLongParam(params, "app_id");
    validateAppSig(appId, params);
    String extId = safeGetParam(params, "uid");
    String filterParam = safeGetParam(params, "filter");
    String _hour = params.get("hour");
    Integer hour = null;
    if (_hour != null)
      hour = parseInt("hour", _hour);
    if (!RE_FILTER.matcher(filterParam).matches())
      throw badValue("filter", filterParam);
    OfferRepository.Filter filter = new OfferRepository.Filter(filterParam);
    Iterable<OfferData> offers = api.getOffers(appId, hour, extId, filter);
    OfferTemplate template;
    String contentType;
    if (format.equals("JSON")) {
      template = jsonTemplate;
      contentType = "application/json; charset=utf-8";
    } else {
      throw badValue("format", format);
    }
    return Response
        .ok(template.render(offers))
        .type(contentType)
        .build();
  }

  private Response doOffer(Map<String, String> params) throws ApiRequestException {
    long appId = safeGetLongParam(params, "app_id");
    validateAppSig(appId, params);
    long offerId = safeGetLongParam(params, "offer_id");
    String extId = safeGetParam(params, "uid");
    return Response.status(302).location(api.doOffer(offerId, appId, extId)).build();
  }

  private Response approveAction(Map<String, String> params) throws ApiRequestException {
    long customerId = safeGetLongParam(params, "customer_id");
    validateCustomerSig(customerId, params);
    long actionId = safeGetLongParam(params, "action_id");
    api.approveAction(customerId, actionId);
    return successResponse();
  }

  @Transactional
  public App validateAppSig(long appId, Map<String, String> params) throws ApiRequestException {
    App app = apps.byId(appId);
    if (app == null)
      throw appNotFound(appId);
    validateSig(app.secret(), params);
    return app;
  }

  @Transactional
  public void validateCustomerSig(long customerId, Map<String, String> params) throws ApiRequestException {
    User user = users.byId(customerId);
    if (user == null)
      throw customerNotFound(customerId);
    if (!user.roles().contains(Role.CUSTOMER))
      throw notInRole(customerId, Role.CUSTOMER);
    validateSig(user.customerSecret(), params);
  }

  private static void validateSig(String secret, Map<String, String> params) throws ApiRequestException {
    String sig = params.remove("sig");
    if (!sign(params, secret).equals(sig))
      throw badSignature(sig);
  }

  private Map<String, String> queryParams() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getQueryParameters().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
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
}
