package com.heymoose.resource;

import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.OfferRepository;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.ApiExceptions.appNotFound;
import static com.heymoose.resource.ApiExceptions.badSignature;
import static com.heymoose.resource.ApiExceptions.badValue;
import static com.heymoose.resource.ApiExceptions.customerNotFound;
import static com.heymoose.resource.ApiExceptions.notInRole;
import static com.heymoose.resource.ApiExceptions.nullParam;
import static com.heymoose.security.Signer.sign;
import com.sun.jersey.api.core.HttpRequestContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
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
  private final BigDecimal compensation;
  private final Provider<UriInfo> uriInfoProvider;
  
  private final static String REQUEST_ID_KEY = "request-id";

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider, AppRepository apps, Api api,
                     @Named("compensation") BigDecimal compensation, UserRepository users, Provider<UriInfo> uriInfoProvider) {
    this.requestContextProvider = requestContextProvider;
    this.apps = apps;
    this.api = api;
    this.compensation = compensation;
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
    else
      throw badValue("method", method);
  }

  private Response introducePerformer(Map<String, String> params) throws ApiRequestException {
    long appId = safeGetLongParam(params, "app_id");
    validateAppSig(appId, params);
    String extId = safeGetParam(params, "uid");
    String _sex = safeGetParam(params, "sex"); // notNull(params.get("sex"));
    if (!asList("MALE", "FEMALE").contains(_sex))
      throw badValue("sex", _sex);
    boolean male = "MALE".equals(_sex);
    Integer year =  safeGetIntParam(params, "year");
    api.introducePerformer(appId, extId, male, year);
    return successResponse();
  }

  private final static Pattern RE_FILTER = Pattern.compile("^\\d+:\\d+(:\\d+x\\d+)?(,\\d+:\\d+(:\\d+x\\d+)?)*$");

  private Response getOffers(String format, Map<String, String> params) throws ApiRequestException {
    long appId = safeGetLongParam(params, "app_id");
    validateAppSig(appId, params);
    String extId = safeGetParam(params, "uid");
    String city = params.get("city");
    String filterParam = safeGetParam(params, "filter");
    if (!RE_FILTER.matcher(filterParam).matches())
      throw badValue("filter", filterParam);
    OfferRepository.Filter filter = new OfferRepository.Filter(filterParam);
    Iterable<Offer> offers = api.getOffers(appId, extId, city, filter);
    OfferTemplate template;
    String contentType;
    if (format.equals("JSON")) {
      template = jsonTemplate;
      contentType = "application/json; charset=utf-8";
    } else {
      throw badValue("format", format);
    }
    return Response
        .ok(template.render(offers, apps.byId(appId), extId, compensation))
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
  public void validateAppSig(long appId, Map<String, String> params) throws ApiRequestException {
    App app = apps.byId(appId);
    if (app == null)
      throw appNotFound(appId);
    validateSig(app.secret(), params);
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
