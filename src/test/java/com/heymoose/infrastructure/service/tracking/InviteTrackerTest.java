package com.heymoose.infrastructure.service.tracking;

import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import org.junit.Test;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class InviteTrackerTest {


  @Test
  public void setsInviteCookie() throws Exception {
    String refererId = "client@cashback.com";
    InviteTracker tracker = new InviteTracker();
    MockRequestContext mockRequest = new MockRequestContext()
      .addQueryParam(InviteTracker.REFERRER_PARAM, refererId)
      .addQueryParam(InviteTracker.LOCATION_PARAM, "http://anything.com");
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
        .addQueryParam(InviteTracker.LOCATION_PARAM, ulp)
        .addQueryParam(InviteTracker.REFERRER_PARAM, "anything");
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
              .addQueryParam(InviteTracker.REFERRER_PARAM, "referer-id")
              .context(),
          new ResponseBuilderImpl());
      fail();
    } catch (ApiRequestException e) {

    }
    try {
      new InviteTracker().track(
          new MockRequestContext()
              .addQueryParam(InviteTracker.LOCATION_PARAM, "http://x.com")
              .context(),
          new ResponseBuilderImpl());
      fail();
    } catch (ApiRequestException e) {

    }

  }
}
