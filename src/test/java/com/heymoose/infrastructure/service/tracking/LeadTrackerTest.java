package com.heymoose.infrastructure.service.tracking;

import com.beust.jcommander.Strings;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.LeadStat;
import com.heymoose.domain.statistics.OfferStat;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.service.OfferLoader;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public final class LeadTrackerTest {

  private static final Logger log =
      LoggerFactory.getLogger(LeadTrackerTest.class);
  private static final String COOKIE_META = "Set-Cookie";

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
    assertFalse("Cookie should not be reset", map.containsKey(COOKIE_META));
  }

  @Test
  public void idCookieIsDifferentEveryTime() throws Exception {
    LeadTracker tracker = new LeadTracker(
        mock(Repo.class),
        mock(OfferLoader.class));
    Response.ResponseBuilder response = new ResponseBuilderImpl();

    tracker.track(MockRequestContext.empty(), response);
    tracker.track(MockRequestContext.empty(), response);
    Set<String> uniqueCookies = Sets.newHashSet();
    for (Object setCookie: response.build().getMetadata().get(COOKIE_META)) {
      Cookie cookie = Cookie.valueOf(setCookie.toString());
      String cookieValue = cookie.getValue();
      assertEquals("Cookie value length should be 32", 32, cookieValue.length());
      uniqueCookies.add(cookieValue);
    }

    assertEquals("Less then 2 unique cookies set", 2, uniqueCookies.size());
  }

  @Test
  public void savesCorrectLeadStatOnClick() throws Exception {
    String idValue = "idValue";
    String referrer = "http://referrer.com";
    String ip = "127.0.0.1";
    Offer offer = offerWithIdAndAdvertiser();
    Token token = createToken();

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

    ArgumentCaptor<LeadStat> leadStatCaptor =
        ArgumentCaptor.forClass(LeadStat.class);
    verify(repo).put(leadStatCaptor.capture());
    LeadStat savedStat = leadStatCaptor.getValue();

    assertEquals(token, savedStat.token());
    assertEquals(token.stat().affiliateId(), savedStat.affId());
    assertEquals(token.stat().master(), savedStat.master());
    assertEquals(ip, savedStat.ip());
    assertEquals(referrer, savedStat.referrer());
    assertEquals(idValue, savedStat.key());
    assertEquals("click", savedStat.method());
  }

  @Test
  public void savesCorrectLeadStatOnAction() throws Exception {
    String idValue = "idValue";
    String referrer = "http://referrer.com";
    String ip = "127.0.0.1";
    Offer offer = offerWithCodeAndAdvertiser();
    Token token = createToken();

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

    ArgumentCaptor<LeadStat> leadStatCaptor =
        ArgumentCaptor.forClass(LeadStat.class);
    verify(repo).put(leadStatCaptor.capture());
    LeadStat savedStat = leadStatCaptor.getValue();

    assertEquals(token, savedStat.token());
    assertEquals(token.stat().affiliateId(), savedStat.affId());
    assertEquals(token.stat().master(), savedStat.master());
    assertEquals(ip, savedStat.ip());
    assertEquals(referrer, savedStat.referrer());
    assertEquals(idValue, savedStat.key());
    assertEquals("reportAction", savedStat.method());
  }

  @Test
  public void userAgentIsTracked() throws Exception {
    String userAgent = "user-agent one two three";
    Offer offer = offerWithIdAndAdvertiser();
    Token token = createToken();
    MockRequestContext mockContext = new MockRequestContext()
        .addQueryParam("offer_id", offer.id().toString())
        .addCookie("hm_token_" + offer.advertiser().id(), token.value())
        .addHeader("User-Agent", userAgent);
    Repo repo = repoWithToken(token);
    LeadTracker tracker = new LeadTracker(repo, loaderWithOffer(offer));

    ResponseBuilderImpl response = new ResponseBuilderImpl();
    tracker.track(mockContext.context(), response);

    ArgumentCaptor<LeadStat> leadStatCaptor =
        ArgumentCaptor.forClass(LeadStat.class);
    verify(repo).put(leadStatCaptor.capture());
    LeadStat savedStat = leadStatCaptor.getValue();

    assertEquals(userAgent, savedStat.userAgent());
  }


  @Test
  public void trackOfferWithPrice() throws Exception {
    Offer offer = offerWithCodeAndAdvertiser();
    Token token = createToken();
    String offerString =
        offer.code() + ":" + BigDecimal.ONE + ","  +
        offer.code() + ":" + BigDecimal.ONE;
    MockRequestContext mockContext = new MockRequestContext()
        .addQueryParam("offer", offerString)
        .addQueryParam("advertiser_id", offer.advertiser().id().toString())
        .addCookie("hm_token_" + offer.advertiser().id(), token.value());
    Repo repo = repoWithToken(token);
    LeadTracker tracker = new LeadTracker(repo, loaderWithOffer(offer));

    ResponseBuilderImpl response = new ResponseBuilderImpl();
    tracker.track(mockContext.context(), response);
    verify(repo).put(any(LeadStat.class));
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

  private Repo repoWithToken(Token token) {
    Repo repo = mock(Repo.class);
    when(repo.byHQL(eq(Token.class), anyString(), eq(token.value())))
        .thenReturn(token);
    return repo;
  }

  private Token createToken() {
    OfferStat stat = new OfferStat()
        .setAffiliateId(11L)
        .setMaster(22L);
    return new Token(stat).setId(33L);
  }
}
