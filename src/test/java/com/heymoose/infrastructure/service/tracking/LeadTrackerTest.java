package com.heymoose.infrastructure.service.tracking;

import com.beust.jcommander.Strings;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.LeadStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.User;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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


  @Test
  public void savesCorrectLeadStatOnClick() throws Exception {
    String idValue = "idValue";
    String referrer = "http://referrer.com";
    String ip = "127.0.0.1";
    Offer offer = offerWithIdAndAdvertiser();
    Token token = new Token(null).setId(1L);

    ArgumentCaptor<LeadStat> leadStatCaptor =
        ArgumentCaptor.forClass(LeadStat.class);
    Repo repo = mock(Repo.class);
    when(repo.get(Offer.class, offer.id())).thenReturn(offer);
    when(repo.byHQL(eq(Token.class), anyString(), eq(token.value()))).thenReturn(token);

    HttpRequestContext context = mock(HttpRequestContext.class);
    MultivaluedMap<String, String> cookieMap =
        mapCopy(ImmutableMap.of(
            LeadTracker.HM_ID_KEY, idValue,
            "hm_token_" + offer.advertiser().id(), token.value()));
    MultivaluedMap<String, String> queryParamMap = mapCopy(
        ImmutableMap.of(
            "method", "click",
            "offer_id", offer.id().toString(),
            offer.tokenParamName(), token.value()));
    when(context.getCookieNameValueMap()).thenReturn(cookieMap);
    when(context.getQueryParameters()).thenReturn(queryParamMap);
    when(context.getHeaderValue("Referer")).thenReturn(referrer);
    when(context.getHeaderValue("X-Real-IP")).thenReturn(ip);

    LeadTracker tracker = new LeadTracker(repo);
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    tracker.track(context, response);

    verify(repo).put(leadStatCaptor.capture());
    LeadStat savedStat = leadStatCaptor.getValue();

    assertEquals(token, savedStat.token());
    assertEquals(ip, savedStat.ip());
    assertEquals(referrer, savedStat.referrer());
    assertEquals(idValue, savedStat.key());
  }

  private MultivaluedMapImpl emptyMap() {
    return new MultivaluedMapImpl();
  }

  private MultivaluedMapImpl mapCopy(Map<String, String> map) {
    MultivaluedMapImpl result = new MultivaluedMapImpl();
    for (Map.Entry<String, String> entry: map.entrySet()) {
      result.putSingle(entry.getKey(), entry.getValue());
    }
    return result;
  }

  private Offer offerWithIdAndAdvertiser() {
    long advId = 1;
    long offerId = 1;
    User adv = new User().setId(advId);
    return new Offer().setAdvertiser(adv)
        .setTokenParamName("hm_token")
        .setId(offerId);
  }
}
