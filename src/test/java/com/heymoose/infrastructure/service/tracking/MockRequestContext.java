package com.heymoose.infrastructure.service.tracking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MockRequestContext {

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
