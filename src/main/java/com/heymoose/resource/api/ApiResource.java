package com.heymoose.resource.api;

import com.google.common.collect.ImmutableSortedMap;
import com.heymoose.domain.errorinfo.ErrorInfoRepository;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.tracking.ActionTracker;
import com.heymoose.infrastructure.service.tracking.CheckPermissionsTracker;
import com.heymoose.infrastructure.service.tracking.ClickTracker;
import com.heymoose.infrastructure.service.tracking.InviteTracker;
import com.heymoose.infrastructure.service.tracking.LeadTracker;
import com.heymoose.infrastructure.service.tracking.PlacementTracker;
import com.heymoose.infrastructure.service.tracking.ShowTracker;
import com.heymoose.infrastructure.service.tracking.Tracker;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;
import static com.heymoose.resource.api.ApiExceptions.badValue;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

@Path("api")
@Singleton
public class ApiResource {

  private final static Logger log = LoggerFactory.getLogger(ApiResource.class);

  private final Provider<HttpRequestContext> requestContextProvider;
  private final ClickTracker clickTracker;
  private final ShowTracker showTracker;
  private final ActionTracker actionTracker;
  private final LeadTracker leadTracker;
  private final InviteTracker inviteTracker;
  private final ErrorInfoRepository errorInfoRepo;
  private final PlacementTracker placementTracker;
  private final CheckPermissionsTracker checkPermissions;

  private final static String REQUEST_ID_KEY = "request-id";

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider,
                     CheckPermissionsTracker checkPermissions,
                     ShowTracker showTracker,
                     ClickTracker clickTracker,
                     ActionTracker actionTracker,
                     LeadTracker leadTracker,
                     InviteTracker inviteTracker,
                     PlacementTracker placementTracker,
                     ErrorInfoRepository errorInfoRepo) {
    this.requestContextProvider = requestContextProvider;
    this.checkPermissions = checkPermissions;
    this.clickTracker = clickTracker;
    this.showTracker = showTracker;
    this.actionTracker = actionTracker;
    this.leadTracker = leadTracker;
    this.inviteTracker = inviteTracker;
    this.errorInfoRepo = errorInfoRepo;
    this.placementTracker = placementTracker;
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

  @GET
  @Path("/click/{id}")
  @Transactional
  public Response clickFromPlacement(@PathParam("id") Long placementId)
      throws ApiRequestException {
    HttpRequestContext context = requestContextProvider.get();
    context
        .getQueryParameters()
        .putSingle("placement_id", placementId.toString());
    placementTracker.track(context, null);
    return callMethod("click");
  }

  @GET
  @Path("/show/{id}")
  @Transactional
  public Response showFromPlacement(@PathParam("id") Long placementId)
      throws ApiRequestException {
    HttpRequestContext context = requestContextProvider.get();
    context
        .getQueryParameters()
        .putSingle("placement_id", placementId.toString());
    placementTracker.track(context, null);
    return callMethod("track");
  }

  private Response callMethodInternal(@QueryParam("method") String method)
      throws ApiRequestException {

    ensureNotNull("method", method);
    if (method.equals("track"))
      return track();
    else if (method.equals("click"))
      return click();
    else if (method.equals("reportAction"))
      return reportAction();
    else if (method.equals("cashbackInvite"))
      return invite();
    else
      throw badValue("method", method);
  }

  @Transactional
  public Response invite() throws ApiRequestException {
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    runTrackers(requestContextProvider.get(), response, inviteTracker);
    return response.build();
  }


  @Transactional
  public Response reportAction() throws ApiRequestException {
    DateTime start = DateTime.now();
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    HttpRequestContext context = requestContextProvider.get();
    try {
      runTrackers(context, response, actionTracker, leadTracker);
      return response.build();
    } finally {
      log.debug("Report Action url: {} time: {}",
          context.getRequestUri(),
          new Duration(start, DateTime.now()));
    }
  }

  @Transactional
  public Response click() throws ApiRequestException {
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    HttpRequestContext context = requestContextProvider.get();
    runTrackers(context, response, checkPermissions, clickTracker, leadTracker);
    return response.build();
  }

  @Transactional
  public Response track() throws ApiRequestException {
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    HttpRequestContext context = requestContextProvider.get();
    runTrackers(context, response, checkPermissions, showTracker);
    return response.build();
  }

  private Response errorResponse(String message, Throwable cause, int status,
                                 boolean includeStackTrace) {
    URI requestUri = requestContextProvider.get().getRequestUri();
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
    Map<String, String> params = queryParams(requestContextProvider.get());

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

  private void runTrackers(HttpRequestContext request,
                           Response.ResponseBuilder response,
                           Tracker... trackerList) throws ApiRequestException {
    for (Tracker tracker : trackerList) {
      if (!tracker.track(request, response)) {
        log.warn("{} failed for request: {}",
            tracker.getClass().getSimpleName(),
            queryParams(request));
        break;
      }
    }
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
