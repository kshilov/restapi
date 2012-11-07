package com.heymoose.infrastructure.service.tracking;

import com.beust.jcommander.Strings;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.heymoose.domain.base.Repo;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public final class LeadTrackerTest {

  private static final Logger log =
      LoggerFactory.getLogger(LeadTrackerTest.class);
  private static final String COOKIE_META = "Set-Cookie";

  @Test
  public void createsLeadIfNoCookie() throws Exception {
    LeadTracker tracker = new LeadTracker(mock(Repo.class));
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    HttpRequestContext context = mock(HttpRequestContext.class);

    when(context.getCookieNameValueMap()).thenReturn(emptyMap());

    tracker.track(context, response);
    log.info("Response: {}", response);

    MultivaluedMap<String, Object> map = response.build().getMetadata();
    Map<String, String> cookie = Splitter
        .on(';').withKeyValueSeparator("=")
        .split(map.getFirst(COOKIE_META).toString());

    assertTrue("Cookie not set", cookie.containsKey(LeadTracker.HM_ID_KEY));
    assertFalse(Strings.isStringEmpty(cookie.get(LeadTracker.HM_ID_KEY)));
  }

  @Test
  public void doesNotResetCookieIfAlreadySet() throws Exception {
    LeadTracker tracker = new LeadTracker(mock(Repo.class));
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    HttpRequestContext context = mock(HttpRequestContext.class);

    MultivaluedMap<String, String> cookieMap = emptyMap();
    cookieMap.put(LeadTracker.HM_ID_KEY, ImmutableList.of("someValue"));
    when(context.getCookieNameValueMap()).thenReturn(cookieMap);

    tracker.track(context, response);
    log.info("Response: {}", response);

    MultivaluedMap<String, Object> map = response.build().getMetadata();
    assertFalse("Cookie should not be reset", map.containsKey("Set-Cookie"));
  }

  private MultivaluedMap<String, String> emptyMap() {
    return new MultivaluedMapImpl();
  }
}
