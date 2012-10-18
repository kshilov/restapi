package com.heymoose.infrastructure.service.tracking;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.resource.api.ApiRequestException;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.heymoose.resource.api.ApiExceptions.*;

public final class TrackingUtils {

  private TrackingUtils() { }

  public static long safeGetLongParam(Map<String, String> params,
                                      String paramName)
      throws ApiRequestException {
    String val = safeGetParam(params, paramName);
    try {
      return Long.valueOf(val);
    } catch (NumberFormatException e) {
      throw badValue(paramName, val);
    }
  }

  public static int safeGetIntParam(Map<String, String> params,
                                    String paramName)
      throws ApiRequestException {
    String val = safeGetParam(params, paramName);
    try {
      return Integer.valueOf(val);
    } catch (NumberFormatException e) {
      throw badValue(paramName, val);
    }
  }

  private static Integer parseInt(String name, String s)
      throws ApiRequestException {
    try {
      return Integer.valueOf(s);
    } catch (NumberFormatException e) {
      throw badValue(name, s);
    }
  }

  private static Long parseLong(String name, String s)
      throws ApiRequestException {
    try {
      return Long.valueOf(s);
    } catch (NumberFormatException e) {
      throw badValue(name, s);
    }
  }


  public static String safeGetParam(Map<String, String> params,
                                    String paramName)
      throws ApiRequestException {
    String val = params.get(paramName);
    ensureNotNull(paramName, val);
    return val;
  }

  public static void ensurePresent(Map<String, String> map, String key)
      throws ApiRequestException {
    if (map.containsKey(key) && map.get(key) != null) return;
    throw nullParam(key);
  }

  public static void ensureNotNull(String paramName, Object value)
      throws ApiRequestException {
    if (value == null)
      throw nullParam(paramName);
  }


  public static Response forbidden(OfferGrant grant) {
    if (grant.backUrl() == null)
      return Response.status(403).build();
    else
      return Response.status(302).location(URI.create(grant.backUrl())).build();
  }


  public static boolean visible(BaseOffer offer) {
    if (offer instanceof Offer) {
      Offer newOffer = (Offer) offer;
      return newOffer.visible();
    } else if (offer instanceof SubOffer) {
      SubOffer subOffer = (SubOffer) offer;
      return subOffer.active();
    } else {
      return false;
    }
  }

  public static Long getRealIp(HttpRequestContext context) {
    String hRealIp = context.getHeaderValue("X-Real-IP");
    if (hRealIp == null)
      return null;
    String[] parts = hRealIp.split("\\.");
    long a = Long.valueOf(parts[0]);
    long b = Long.valueOf(parts[1]);
    long c = Long.valueOf(parts[2]);
    long d = Long.valueOf(parts[3]);
    return (a << 24) | (b << 16) | (c << 8) | d;
  }


  public static String extractReferer(HttpRequestContext context) {
    return Objects.firstNonNull(
        context.getHeaderValue("Referer"),
        context.getHeaderValue("X-Real-IP"));
  }


  public static void addCookie(Response.ResponseBuilder resp, String name,
                               String value, int age) {
    long expires = System.currentTimeMillis() + age * 1000L;
    resp.header("Set-Cookie", String
        .format("%s=%s;Version=1;expires=%s;Path=/", name, value,
            formatAsGMT(expires)));
  }

  private static String formatAsGMT(long time) {
    // Wed, 19 Jan 2011 12:05:26 GMT
    SimpleDateFormat format = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    return format.format(new Date(time));
  }


  public static Response.ResponseBuilder noCache(
      Response.ResponseBuilder response) {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge(0);
    cacheControl.setNoCache(true);
    return response.cacheControl(cacheControl);
  }

  public static Map<String, String> queryParams(HttpRequestContext context) {
    Map<String, String> params = Maps.newHashMap();
    for (Map.Entry<String, List<String>> ent :
        context.getQueryParameters().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
  }

  public static Token checkToken(Repo repo, String tokenValue) {
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", tokenValue);
    if (token == null) throw new IllegalArgumentException("Token [" +
        tokenValue + " ] not found.");

    return token;
  }


}
