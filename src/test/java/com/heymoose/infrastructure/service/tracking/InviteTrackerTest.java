package com.heymoose.infrastructure.service.tracking;

import com.google.common.base.Strings;
import com.heymoose.resource.api.ApiExceptions;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.heymoose.infrastructure.service.tracking.TrackingUtils.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class InviteTrackerTest {

  private static class InviteTracker implements Tracker {

    public static final String COOKIE_NAME = "hm_invite";

    public void track(HttpRequestContext context,
                      Response.ResponseBuilder response)
        throws ApiRequestException {
      Map<String, String> queryParams = queryParams(context);
      String referer = queryParams.get("referer");
      String ulp = queryParams.get("ulp");

      if (Strings.isNullOrEmpty(referer))
        throw ApiExceptions.nullParam("referer");
      if (Strings.isNullOrEmpty(ulp))
        throw ApiExceptions.nullParam("ulp");

      addCookie(response, COOKIE_NAME, referer, Integer.MAX_VALUE);
      response.status(302).header("Location", ulp);
    }
  }


  @Test
  public void setsInviteCookie() throws Exception {
    String refererId = "client@cashback.com";
    InviteTracker tracker = new InviteTracker();
    MockRequestContext mockRequest = new MockRequestContext()
      .addQueryParam("referer", refererId)
      .addQueryParam("ulp", "http://anything.com");
    Response.ResponseBuilder response = new ResponseBuilderImpl();

    tracker.track(mockRequest.context(), response);

    Cookie cookie = Cookie.valueOf(response
        .build()
        .getMetadata()
        .get("Set-Cookie").get(0).toString());

    assertEquals(InviteTracker.COOKIE_NAME, cookie.getName());
    assertEquals(refererId, cookie.getValue());
  }

  @Test
  public void redirectsToUlp() throws Exception {
    String ulp = "http://ulp.com";
    InviteTracker tracker = new InviteTracker();
    MockRequestContext mockRequestContext = new MockRequestContext()
        .addQueryParam("ulp", ulp)
        .addQueryParam("referer", "anything");
    Response.ResponseBuilder responseBuilder = new ResponseBuilderImpl();

    tracker.track(mockRequestContext.context(), responseBuilder);

    Response response = responseBuilder.build();
    assertEquals(302, response.getStatus());
    assertEquals(ulp, response.getMetadata().get("Location").get(0).toString());
  }

  @Test
  public void throwsApiRequestExceptionIfAnyParameterIsAbsent() throws Exception {
    try {
      new InviteTracker().track(
          new MockRequestContext()
              .addQueryParam("referer", "referer-id")
              .context(),
          new ResponseBuilderImpl());
      fail();
    } catch (ApiRequestException e) {

    }
    try {
      new InviteTracker().track(
          new MockRequestContext()
              .addQueryParam("ulp", "http:://x.com")
              .context(),
          new ResponseBuilderImpl());
      fail();
    } catch (ApiRequestException e) {

    }

  }
}
