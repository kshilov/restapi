package com.heymoose.resource;

import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.Platform;
import com.sun.jersey.api.core.HttpRequestContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.heymoose.security.Signer.sign;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import static java.util.Arrays.asList;

@Path("api")
@Singleton
public class ApiResource {

  private final Provider<HttpRequestContext> requestContextProvider;
  private final AppRepository apps;
  private final OfferTemplate htmlTemplate = new HtmlOfferTemplate();
  private final OfferTemplate jsonTemplate = new JsonOfferTemplate();
  private final Api api;
  private final BigDecimal compensation;

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider, AppRepository apps, Api api,
                     @Named("compensation") BigDecimal compensation) {
    this.requestContextProvider = requestContextProvider;
    this.apps = apps;
    this.api = api;
    this.compensation = compensation;
  }

  @GET
  public Response callMethod(@QueryParam("method") String method,
                             @QueryParam("app_id") Long appId,
                             @QueryParam("sig") String sig,
                             @QueryParam("format") @DefaultValue("HTML") String format) {
    checkNotNull(method, appId, sig);
    if (!asList("HTML", "JSON").contains(format))
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    validateSig();
    Map<String, String> params = queryParams();
    if (method.equals("getOffers")) {
      String extId = notNull(params.get("uid"));
      Iterable<Offer> offers = api.getOffers(appId, extId);
      OfferTemplate template;
      String contentType;
      if (format.equals("JSON")) {
        template = jsonTemplate;
        contentType = "application/json; charset=utf-8";
      } else if (format.equals("HTML")) {
        template = htmlTemplate;
        contentType = "text/html; charset=utf-8";
      } else {
        throw new IllegalStateException();
      }
      return Response
          .ok(template.render(offers, apps.byId(appId), extId, compensation))
          .type(contentType)
          .build();
    } else if (method.equals("doOffer")) {
      long offerId = longFrom(params.get("offer_id"));
      String extId = notNull(params.get("uid"));
      Platform platform = platform(params.get("platform"));
      return Response.status(302).location(api.doOffer(offerId, appId, extId, platform)).build();
    } else {
      throw new IllegalStateException();
    }
  }

  private void validateSig() {
    Map<String, String> params = queryParams();
    long appId = longFrom(params.get("app_id"));
    App app = apps.byId(appId);
    if (app == null)
      throw unauthorized();
    String sig = params.remove("sig");
    if (!sign(params, app.secret()).equals(sig))
      throw unauthorized();
  }

  private Map<String, String> queryParams() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getQueryParameters().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
  }

  private static WebApplicationException unauthorized() {
    return new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

  private static <T> T notNull(T arg) {
    if (arg == null)
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    return arg;
  }

  private static long longFrom(String from) {
    from = notNull(from);
    try {
      return Long.valueOf(from);
    } catch (NumberFormatException e) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  private static Platform platform(String platform) {
    platform = notNull(platform);
    try {
      return Platform.valueOf(platform);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
}
