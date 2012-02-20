package com.heymoose.resource;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.App;
import com.heymoose.domain.AppFilterType;
import com.heymoose.domain.AppRepository;
import com.heymoose.domain.AppTargeting;
import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerOffer;
import com.heymoose.domain.BannerSize;
import com.heymoose.domain.BannerSizeRepository;
import com.heymoose.domain.CityFilterType;
import com.heymoose.domain.CityTargeting;
import com.heymoose.domain.City;
import com.heymoose.domain.CityRepository;
import com.heymoose.domain.Offer;
import com.heymoose.domain.Order;
import com.heymoose.domain.OrderRepository;
import com.heymoose.domain.OrderRepository.Ordering;
import com.heymoose.domain.RegularOffer;
import com.heymoose.domain.Targeting;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.VideoOffer;
import com.heymoose.domain.settings.Settings;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.badRequest;
import static com.heymoose.resource.Exceptions.notFound;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;
import com.heymoose.resource.xml.XmlOrders;
import com.heymoose.util.HibernateUtil;
import static com.heymoose.util.HibernateUtil.unproxy;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.math.BigDecimal;

import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("orders")
@Singleton
public class OrderResource {

  private final UserRepository users;
  private final OrderRepository orders;
  private final Accounts accounts;
  private final BannerSizeRepository bannerSizes;
  private final CityRepository cities;
  private final AppRepository apps;
  private final Settings settings;

  @Inject
  public OrderResource(UserRepository users, OrderRepository orders, Accounts accounts,
                       BannerSizeRepository bannerSizes, CityRepository cities, AppRepository apps, Settings settings) {
    this.users = users;
    this.orders = orders;
    this.accounts = accounts;
    this.bannerSizes = bannerSizes;
    this.cities = cities;
    this.apps = apps;
    this.settings = settings;
  }

  @GET
  @Transactional
  public XmlOrders list(@QueryParam("offset") @DefaultValue("0") int offset,
                        @QueryParam("limit") @DefaultValue("20") int limit,
                        @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                        @QueryParam("asc") @DefaultValue("false") boolean asc,
                        @QueryParam("full") @DefaultValue("false") boolean full,
                        @QueryParam("userId") Long userId) {
    
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Mappers.toXmlOrders(accounts, orders.list(ord, asc, offset, limit, userId), d);
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count(@QueryParam("userId") Long userId) {
    return Response.ok(Mappers.toXmlCount(orders.count(userId))).build();
  }

  @POST
  @Transactional
  public Response create(@FormParam("userId") int userId,
                         @FormParam("title") String title,
                         @FormParam("url") String url,
                         @FormParam("balance") String _balance,
                         @FormParam("cpa") String _cpa,
                         @FormParam("autoApprove") @DefaultValue("false") boolean autoApprove,
                         @FormParam("allowNegativeBalance") Boolean allowNegativeBalance,
                         @FormParam("male") Boolean male,
                         @FormParam("minAge") Integer minAge,
                         @FormParam("maxAge") Integer maxAge,
                         @FormParam("cityFilterType") CityFilterType cityFilterType,
                         @FormParam("city") List<Long> cityIds,
                         @FormParam("appFilterType") AppFilterType appFilterType,
                         @FormParam("app") List<Long> appIds,
                         @FormParam("minHour") Integer minHour,
                         @FormParam("maxHour") Integer maxHour,
                         @FormParam("reentrant") @DefaultValue("false") boolean reentrant,
                         @FormParam("type") Offer.Type type,
                         @FormParam("image") String image,
                         @FormParam("description") String description,
                         @FormParam("videoUrl") String videoUrl,
                         @FormParam("bannerMimeType") String bannerMimeType,
                         @FormParam("bannerSize") Integer bannerSizeId) {

    checkNotNull(_balance, _cpa);

    BigDecimal cpa;
    BigDecimal balance;

    try {
      cpa = new BigDecimal(_cpa);
    } catch (Exception e) {
      return Response.status(400).build();
    }

    try {
      balance = new BigDecimal(_balance);
    } catch (Exception e) {
      return Response.status(400).build();
    }

    if (cpa.signum() != 1 || balance.signum() < 0)
      return Response.status(400).build();

    if (cpa.compareTo(settings.Cmin()) < 0)
      return Response.status(400).build();

    if (minAge != null && minAge < 0)
      return Response.status(400).build();

    if (maxAge != null && maxAge < 0)
      return Response.status(400).build();

    CityTargeting cityTargeting = null;
    if (cityFilterType != null) {
      Map<Long, City> cityMap = cities.byIds(cityIds);
      cityTargeting = new CityTargeting(cityFilterType, cityMap.values());
    }

    AppTargeting appTargeting = null;
    if (appFilterType != null) {
      Map<Long, App> appMap = apps.byIds(appIds);
      appTargeting = new AppTargeting(appFilterType, appMap.values());
    }

    User user = users.byId(userId);
    if (user == null)
      return Response.status(404).build();
    
    if (!user.isCustomer())
      return Response.status(Response.Status.CONFLICT).build();

    DateTime now = DateTime.now();

    Offer offer;
    if (type.equals(Offer.Type.REGULAR)) {
      offer = new RegularOffer(title, url, autoApprove, now, reentrant, description, image);
    } else if (type.equals(Offer.Type.VIDEO)) {
      offer = new VideoOffer(title, url, autoApprove, now, reentrant, videoUrl);
    } else if (type.equals(Offer.Type.BANNER)) {
      checkNotNull(bannerSizeId, bannerMimeType);
      BannerSize size = bannerSizes.byId(bannerSizeId);
      if (size == null)
        return Response.status(404).build();
      if (size.disabled())
        return Response.status(409).build();
      Banner banner = new Banner(image, bannerMimeType, size);
      offer = new BannerOffer(title, url, autoApprove, now, reentrant, asList(banner));
    } else {
      throw new IllegalArgumentException("Unknown type: " + type.name());
    }

    Targeting targeting = new Targeting(male, minAge, maxAge, cityTargeting, appTargeting, minHour, maxHour);
    Order order = new Order(offer, cpa, user, now, allowNegativeBalance, targeting);
    
    BigDecimal amount = balance;

    if (balance.signum() > 0 && accounts.lastTxOf(user.customerAccount()).balance().compareTo(amount) == -1)
      return Response.status(409).build();

    orders.put(order);
    
    if (balance.signum() > 0)
      accounts.transfer(user.customerAccount(), order.account(), amount);

    return Response.ok(Long.toString(order.id())).build();
  }

  @POST
  @Path("{id}/banners")
  @Transactional
  public void addBanner(@PathParam("id") long orderId,
                        @FormParam("bannerSize") Integer bannerSizeId,
                        @FormParam("mimeType") String mimeType,
                        @FormParam("image") String image) {
    checkNotNull(bannerSizeId, mimeType, image);
    Order order = orders.byId(orderId);
    if (order == null)
      throw notFound();
    Offer offer = unproxy(order.offer());
    if (!(offer instanceof BannerOffer))
      throw badRequest();
    BannerOffer bannerOffer = (BannerOffer) offer;
    BannerSize size = bannerSizes.byId(bannerSizeId);
    if (size == null)
       throw notFound();
    Banner banner = new Banner(image, mimeType, size);
    bannerOffer.addBanner(banner);
  }

  @DELETE
  @Path("{id}/banners/{bannerId}")
  @Transactional
  public void deleteBanner(@PathParam("id") long orderId, @PathParam("bannerId") long bannerId) {
    Order order = orders.byId(orderId);
    if (order == null)
      throw notFound();
    Offer offer = unproxy(order.offer());
    if (!(offer instanceof BannerOffer))
      throw badRequest();
    BannerOffer bannerOffer = (BannerOffer) offer;
    boolean found = false;
    for (Banner banner : bannerOffer.banners()) {
      if (banner.id().equals(bannerId)) {
        found = true;
        bannerOffer.deleteBanner(banner);
      }
    }
    if (!found)
      throw badRequest();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlOrder(accounts, order, Details.WITH_RELATED_ENTITIES)).build();
  }
  
  @PUT
  @Path("{id}")
  @Transactional
  public Response update(@Context HttpContext context, @PathParam("id") long id) {
    Order order = existing(id);
    Form params = context.getRequest().getEntity(Form.class);
    
    if (params.containsKey("cpa")) {
      BigDecimal cpa;
      try {
        cpa = new BigDecimal(params.getFirst("cpa"));
      }
      catch (Exception e) {
        throw new WebApplicationException(400);
      }
      if (cpa.signum() != 1)
        throw new WebApplicationException(400);
      order.setCpa(cpa);
    }
    
    if (params.containsKey("allowNegativeBalance"))
      order.account().setAllowNegativeBalance(Boolean.valueOf(params.getFirst("allowNegativeBalance")));
    if (params.containsKey("title"))
      order.offer().setTitle(params.getFirst("title"));
    if (params.containsKey("url"))
      order.offer().setUrl(params.getFirst("url"));
    if (params.containsKey("autoApprove"))
      order.offer().setAutoApprove(Boolean.valueOf(params.getFirst("autoApprove")));
    if (params.containsKey("reentrant"))
      order.offer().setReentrant(Boolean.valueOf(params.getFirst("reentrant")));
    if (params.containsKey("male"))
      order.targeting().setMale(toBoolean(params.getFirst("male")));
    if (params.containsKey("minAge"))
      order.targeting().setMinAge(toInteger(params.getFirst("minAge")));
    if (params.containsKey("maxAge"))
      order.targeting().setMaxAge(toInteger(params.getFirst("maxAge")));
    if (params.containsKey("minHour"))
      order.targeting().setMinHour(toInteger(params.getFirst("minHour")));
    if (params.containsKey("maxHour"))
      order.targeting().setMaxHour(toInteger(params.getFirst("maxHour")));
    if (params.containsKey("cityFilterType")) {
      String filterTypeParam = params.getFirst("cityFilterType");
      CityFilterType filterType = null;
      if (!isNull(filterTypeParam))
        filterType = Enum.valueOf(CityFilterType.class, filterTypeParam);
      order.targeting().setCityFilterType(filterType);
    }
    if (params.containsKey("city")) {
      List<Long> ids = new ArrayList<Long>();
      for (String cityId : params.get("city"))
        ids.add(Long.valueOf(cityId));
      order.targeting().cities().clear();
      order.targeting().cities().addAll(cities.byIds(ids).values());
    }
    if (params.containsKey("appFilterType")) {
      String filterTypeParam = params.getFirst("appFilterType");
      AppFilterType filterType = null;
      if (!isNull(filterTypeParam))
        filterType = Enum.valueOf(AppFilterType.class, filterTypeParam);
      else
        order.targeting().apps().clear();
      order.targeting().setAppFilterType(filterType);
    }
    if (params.containsKey("app")) {
      List<Long> ids = new ArrayList<Long>();
      for (String appId : params.get("app"))
        ids.add(Long.valueOf(appId));
      order.targeting().apps().clear();
      order.targeting().apps().addAll(apps.byIds(ids).values());
    }
    
    Offer offer = HibernateUtil.unproxy(order.offer());
    
    if (offer instanceof RegularOffer) {
      RegularOffer regularOffer = (RegularOffer)offer;
      if (params.containsKey("image"))
        regularOffer.setImageBase64(params.getFirst("image"));
      if (params.containsKey("description"))
        regularOffer.setDescription(params.getFirst("description"));
    }
    
    if (offer instanceof VideoOffer) {
      VideoOffer videoOffer = (VideoOffer)offer;
      if (params.containsKey("videoUrl"))
        videoOffer.setVideoUrl(params.getFirst("videoUrl"));
    }
    
    return Response.ok().build();
  }

  @PUT
  @Path("{id}/enabled")
  @Transactional
  public Response enable(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.enable();
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}/enabled")
  @Transactional
  public Response disable(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.disable();
    return Response.ok().build();
  }


  @PUT
  @Path("{id}/paused")
  @Transactional
  public Response pause(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.pause();
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}/paused")
  @Transactional
  public Response play(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    order.play();
    return Response.ok().build();
  }
  
  private Order existing(long id) {
    Order order = orders.byId(id);
    if (order == null)
      throw new WebApplicationException(404);
    return order;
  }
  
  private Boolean toBoolean(String param) {
    if (isNull(param)) return null;
    return Boolean.valueOf(param);
  }
  
  private Integer toInteger(String param) {
    if (isNull(param)) return null;
    return Integer.valueOf(param);
  }
  
  private boolean isNull(String param) {
    String paramLower = param.toLowerCase();
    return paramLower.isEmpty() || paramLower.equals("null") || paramLower.equals("none");
  }
}
