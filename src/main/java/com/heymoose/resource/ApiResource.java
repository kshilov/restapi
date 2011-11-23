package com.heymoose.resource;

import static com.google.common.collect.Maps.newHashMap;
import com.heymoose.domain.App;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.Platform;
import com.heymoose.domain.Role;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.badRequest;
import static com.heymoose.resource.Exceptions.unauthorized;
import static com.heymoose.security.Signer.sign;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import com.sun.jersey.api.core.HttpRequestContext;
import java.math.BigDecimal;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("api")
@Singleton
public class ApiResource {

  private final Provider<HttpRequestContext> requestContextProvider;
  private final AppRepository apps;
  private final UserRepository users;
  private final OfferTemplate jsonTemplate = new JsonOfferTemplate();
  private final Api api;
  private final BigDecimal compensation;

  @Inject
  public ApiResource(Provider<HttpRequestContext> requestContextProvider, AppRepository apps, Api api,
                     @Named("compensation") BigDecimal compensation, UserRepository users) {
    this.requestContextProvider = requestContextProvider;
    this.apps = apps;
    this.api = api;
    this.compensation = compensation;
    this.users = users;
  }

  @GET
  public Response callMethod(@QueryParam("method") String method,
                             @QueryParam("sig") String sig,
                             @QueryParam("format") @DefaultValue("HTML") String format) {
    checkNotNull(method, sig);
    if (!asList("HTML", "JSON").contains(format))
      throw badRequest();
    Map<String, String> params = queryParams();
    if (method.equals("getOffers"))
      return getOffers(format, params);
    else if (method.equals("doOffer"))
      return doOffer(params);
    else if (method.equals("approveAction"))
      return approveAction(params);
    else if (method.equals("introducePerformer"))
      return introducePerformer(params);
    else
      throw badRequest();
  }

  private Response introducePerformer(Map<String, String> params) {
    long appId = longFrom(params.get("app_id"));
    validateAppSig(appId, params);
    String extId = notNull(params.get("uid"));
    String _sex = notNull(params.get("sex"));
    if (!asList("MALE", "FEMALE").contains(_sex))
      throw badRequest();
    boolean male = "MALE".equals(_sex);
    Integer year = intFrom(params.get("year"));
    api.introducePerformer(appId, extId, male, year);
    return Response.ok().build();
  }

  private Response getOffers(String format, Map<String, String> params) {
    long appId = longFrom(params.get("app_id"));
    validateAppSig(appId, params);
    String extId = notNull(params.get("uid"));
    Iterable<Offer> offers = api.getOffers(appId, extId);
    OfferTemplate template;
    String contentType;
    if (format.equals("JSON")) {
      template = jsonTemplate;
      contentType = "application/json; charset=utf-8";
    } else {
      throw badRequest();
    }
    return Response
        .ok(template.render(offers, apps.byId(appId), extId, compensation))
        .type(contentType)
        .build();
  }

  private Response doOffer(Map<String, String> params) {
    long appId = longFrom(params.get("app_id"));
    validateAppSig(appId, params);
    long offerId = longFrom(params.get("offer_id"));
    String extId = notNull(params.get("uid"));
    return Response.status(302).location(api.doOffer(offerId, appId, extId)).build();
  }

  private Response approveAction(Map<String, String> params) {
    long customerId = longFrom(params.get("customer_id"));
    validateCustomerSig(customerId, params);
    long actionId = longFrom(params.get("action_id"));
    api.approveAction(customerId, actionId);
    return Response.ok().build();
  }

  @Transactional
  public void validateAppSig(long appId, Map<String, String> params) {
    App app = apps.byId(appId);
    if (app == null)
      throw unauthorized();
    validateSig(app.secret(), params);
  }

  @Transactional
  public void validateCustomerSig(long customerId, Map<String, String> params) {
    User user = users.byId(customerId);
    if (user == null)
      throw unauthorized();
    if (!user.roles().contains(Role.CUSTOMER))
      throw unauthorized();
    validateSig(user.customerSecret(), params);
  }

  private static void validateSig(String secret, Map<String, String> params) {
    String sig = params.remove("sig");
    if (!sign(params, secret).equals(sig))
      throw unauthorized();
  }

  private Map<String, String> queryParams() {
    Map<String, String> params = newHashMap();
    for (Map.Entry<String, List<String>> ent : requestContextProvider.get().getQueryParameters().entrySet())
      if (!ent.getValue().isEmpty())
        params.put(ent.getKey(), ent.getValue().get(0));
    return params;
  }

  private static <T> T notNull(T arg) {
    if (arg == null)
      throw badRequest();
    return arg;
  }

  private static long longFrom(String from) {
    from = notNull(from);
    try {
      return Long.valueOf(from);
    } catch (NumberFormatException e) {
      throw badRequest();
    }
  }

  private static int intFrom(String from) {
    from = notNull(from);
    try {
      return Integer.valueOf(from);
    } catch (NumberFormatException e) {
      throw badRequest();
    }
  }

  private static Platform platform(String platform) {
    platform = notNull(platform);
    try {
      return Platform.valueOf(platform);
    } catch (IllegalArgumentException e) {
      throw badRequest();
    }
  }
}
