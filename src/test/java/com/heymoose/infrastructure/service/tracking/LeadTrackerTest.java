package com.heymoose.infrastructure.service.tracking;

import com.beust.jcommander.Strings;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.LeadStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.service.OfferLoader;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public final class LeadTrackerTest {

  private static final Logger log =
      LoggerFactory.getLogger(LeadTrackerTest.class);
  private static final String COOKIE_META = "Set-Cookie";

  private static class MockRequestContext {

    public static HttpRequestContext empty() {
      return new MockRequestContext().context();
    }

    private ImmutableMultimap.Builder<String, String> cookieMap;
    private ImmutableMultimap.Builder<String, String> queryParamMap;
    private ImmutableMap.Builder<String, String> headerMap;

    public MockRequestContext() {
      this.cookieMap = ImmutableMultimap.builder();
      this.queryParamMap = ImmutableMultimap.builder();
      this.headerMap = ImmutableMap.builder();
    }

    public MockRequestContext addCookie(String key, String value) {
      this.cookieMap.put(key, value);
      return this;
    }

    public MockRequestContext addQueryParam(String key, String value) {
      this.queryParamMap.put(key, value);
      return this;
    }

    public MockRequestContext addHeader(String key, String value) {
      this.headerMap.put(key, value);
      return this;
    }

    public HttpRequestContext context() {
      HttpRequestContext context = mock(HttpRequestContext.class);
      when(context.getCookieNameValueMap())
          .thenReturn(toMultivaluedMap(cookieMap.build()));
      when(context.getQueryParameters())
          .thenReturn(toMultivaluedMap(queryParamMap.build()));
      Map<String, String> headers = headerMap.build();
      for (String header : headers.keySet()) {
        when(context.getHeaderValue(header)).thenReturn(headers.get(header));
      }
      return context;
    }

    private MultivaluedMapImpl toMultivaluedMap(
        Multimap<String, String> multiMap) {
      MultivaluedMapImpl nameValue = new MultivaluedMapImpl();
      for (String key : multiMap.keySet()) {
        nameValue.put(key, ImmutableList.copyOf(multiMap.get(key)));
      }
      return nameValue;
    }

  }

  @Test
  public void createsLeadIfNoCookie() throws Exception {
    LeadTracker tracker = new LeadTracker(
        mock(Repo.class),
        mock(OfferLoader.class));
    Response.ResponseBuilder response = new ResponseBuilderImpl();

    tracker.track(MockRequestContext.empty(), response);
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
    LeadTracker tracker = new LeadTracker(
        mock(Repo.class),
        mock(OfferLoader.class));
    Response.ResponseBuilder response = new ResponseBuilderImpl();

    MockRequestContext contextMock = new MockRequestContext()
        .addCookie(LeadTracker.HM_ID_KEY, "someValue");

    tracker.track(contextMock.context(), response);
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
    when(repo.byHQL(eq(Token.class), anyString(), eq(token.value())))
        .thenReturn(token);

    MockRequestContext mockContext = new MockRequestContext()
        .addCookie(LeadTracker.HM_ID_KEY, idValue)
        .addQueryParam("method", "click")
        .addQueryParam("offer_id", offer.id().toString())
        .addHeader("Referer", referrer)
        .addHeader("X-Real-IP", ip);

    LeadTracker tracker = new LeadTracker(repo, loaderWithOffer(offer));
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    // this cookie will be set in ClickTracker
    NewCookie hmTokenCookie =
        new NewCookie("hm_token_" + offer.advertiser().id(), token.value());
    response.cookie(hmTokenCookie);
    tracker.track(mockContext.context(), response);

    verify(repo).put(leadStatCaptor.capture());
    LeadStat savedStat = leadStatCaptor.getValue();

    assertEquals(token, savedStat.token());
    assertEquals(ip, savedStat.ip());
    assertEquals(referrer, savedStat.referrer());
    assertEquals(idValue, savedStat.key());
  }

  @Test
  public void savesCorrectLeadStatOnAction() throws Exception {
    String idValue = "idValue";
    String referrer = "http://referrer.com";
    String ip = "127.0.0.1";
    Offer offer = offerWithCodeAndAdvertiser();
    Token token = new Token(null).setId(1L);

    ArgumentCaptor<LeadStat> leadStatCaptor =
        ArgumentCaptor.forClass(LeadStat.class);
    Repo repo = mock(Repo.class);
    when(repo.byHQL(eq(Token.class), anyString(), eq(token.value())))
        .thenReturn(token);

    MockRequestContext mockContext = new MockRequestContext()
        .addCookie(LeadTracker.HM_ID_KEY, idValue)
        .addCookie("hm_token_" + offer.advertiser().id(), token.value())
        .addQueryParam("method", "reportAction")
        .addQueryParam("offer", offer.code())
        .addQueryParam("advertiser_id", offer.advertiser().id().toString())
        .addHeader("Referer", referrer)
        .addHeader("X-Real-IP", ip);

    LeadTracker tracker = new LeadTracker(repo, loaderWithOffer(offer));
    Response.ResponseBuilder response = new ResponseBuilderImpl();
    tracker.track(mockContext.context(), response);

    verify(repo).put(leadStatCaptor.capture());
    LeadStat savedStat = leadStatCaptor.getValue();

    assertEquals(token, savedStat.token());
    assertEquals(ip, savedStat.ip());
    assertEquals(referrer, savedStat.referrer());
    assertEquals(idValue, savedStat.key());
  }


  private Offer offerWithIdAndAdvertiser() {
    long advId = 1;
    long offerId = 1;
    User adv = new User().setId(advId);
    return new Offer().setAdvertiser(adv)
        .setTokenParamName("hm_token")
        .setId(offerId);
  }

  private Offer offerWithCodeAndAdvertiser() {
    return new Offer()
        .setAdvertiser(new User().setId(1L))
        .setCode("offer-code");
  }

  private OfferLoader loaderWithOffer(Offer offer) {
    OfferLoader loader = mock(OfferLoader.class);
    when(loader.findOffer(offer.advertiser().id(), offer.code()))
        .thenReturn(offer);
    when(loader.offerById(offer.id())).thenReturn(offer);
    return loader;
  }
}
