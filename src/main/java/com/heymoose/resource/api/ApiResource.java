package com.heymoose.resource.api;

import com.google.common.collect.ImmutableSortedMap;
import com.heymoose.domain.errorinfo.ErrorInfoRepository;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.tracking.ActionTracker;
import com.heymoose.infrastructure.service.tracking.ClickTracker;
import com.heymoose.infrastructure.service.tracking.ShowTracker;
import com.sun.jersey.api.core.HttpRequestContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
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
import javax.ws.rs.core.Response;
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
import static com.heymoose.infrastructure.service.tracking.TrackingUtils.ensureNotNull;
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
  private final ErrorInfoRepository errorInfoRepo;

  private final static String REQUEST_ID_KEY = "request-id";

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider,
                     ShowTracker showTracker,
                     ClickTracker clickTracker,
                     ActionTracker actionTracker,
                     ErrorInfoRepository errorInfoRepo) {
    this.requestContextProvider = requestContextProvider;
    this.clickTracker = clickTracker;
    this.showTracker = showTracker;
    this.actionTracker = actionTracker;
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

  private Response callMethodInternal(@QueryParam("method") String method)
      throws ApiRequestException {

    ensureNotNull("method", method);
    if (method.equals("track"))
      return track();
    else if (method.equals("click"))
      return click();
    else if (method.equals("reportAction"))
      return reportAction();
    else
      throw badValue("method", method);
  }

  @Transactional
  public Response reportAction() throws ApiRequestException {
    return actionTracker.track(requestContextProvider.get());
  }

  @Transactional
  public Response click() throws ApiRequestException {
    return clickTracker.track(requestContextProvider.get());
  }

  @Transactional
  public Response track() throws ApiRequestException {
    return showTracker.track(requestContextProvider.get());
  }

  private Map<String, String> queryParams() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get()
        .getQueryParameters().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
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

  private static String formatAsGMT(long time) {
    // Wed, 19 Jan 2011 12:05:26 GMT
    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    return format.format(new Date(time));
  }
}
