package com.heymoose.resource;

import com.heymoose.domain.Accounts;
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
import com.heymoose.domain.RegularOffer;
import com.heymoose.domain.Targeting;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.VideoOffer;
import com.heymoose.domain.base.Repository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.Mappers.Details;
import com.heymoose.util.HibernateUtil;
import com.heymoose.util.WebAppUtil;
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

  @Inject
  public OrderResource(UserRepository users, OrderRepository orders, Accounts accounts,
                       BannerSizeRepository bannerSizes, CityRepository cities) {
    this.users = users;
    this.orders = orders;
    this.accounts = accounts;
    this.bannerSizes = bannerSizes;
    this.cities = cities;
  }

  @GET
  @Transactional
  public Response list(@QueryParam("ord") @DefaultValue("creation-time") String ord,
                       @QueryParam("dir") @DefaultValue("desc") String dir,
                       @QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("20") int limit,
                       @QueryParam("full") @DefaultValue("false") boolean full) {
    Details d = full ? Details.WITH_RELATED_ENTITIES : Details.WITH_RELATED_IDS;
    return Response.ok(Mappers.toXmlOrders(orders.list(
        WebAppUtil.queryParamToEnum(ord, OrderRepository.Ordering.CREATION_TIME),
        WebAppUtil.queryParamToEnum(dir, Repository.Direction.DESC),
        offset, limit, 0), d)).build();
  }
  
  @GET
  @Path("count")
  @Transactional
  public Response count() {
    return Response.ok(Mappers.toXmlCount(orders.count())).build();
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
                         @FormParam("reentrant") @DefaultValue("false") boolean reentrant,
                         @FormParam("type") Offer.Type type,
                         @FormParam("image") String image,
                         @FormParam("description") String description,
                         @FormParam("videoUrl") String videoUrl,
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

    if (cpa.signum() != 1 || balance.signum() != 1)
      return Response.status(400).build();

    if (cpa.compareTo(balance) == 1)
      return Response.status(400).build();

    if (minAge != null && minAge < 0)
      return Response.status(400).build();

    if (maxAge != null && maxAge < 0)
      return Response.status(400).build();

    CityTargeting cityTargeting = null;
    if (cityFilterType != null) {
      Map<String, City> cityMap = cities.byIds(cityIds);
      cityTargeting = new CityTargeting(cityFilterType, cityMap.values());
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
      checkNotNull(bannerSizeId);
      BannerSize size = bannerSizes.byId(bannerSizeId);
      if (size == null)
        return Response.status(404).build();
      Banner banner = new Banner(image, size);
      offer = new BannerOffer(title, url, autoApprove, now, reentrant, asList(banner));
    } else {
      throw new IllegalArgumentException("Unknown type: " + type.name());
    }
    
    Targeting targeting = new Targeting(male, minAge, maxAge, cityTargeting);
    Order order = new Order(offer, cpa, user, now, allowNegativeBalance, targeting);
    
    BigDecimal amount = balance;

    if (user.customerAccount().currentState().balance().compareTo(amount) == -1)
      return Response.status(409).build();

    String desc = String.format("Transfering %s from %s to %s", _balance, user.customerAccount().id(), order.account().id());
    accounts.lock(user.customerAccount());
    user.customerAccount().subtractFromBalance(amount, desc);
    order.account().addToBalance(amount, desc);

    orders.put(order);
    return Response.ok(Long.toString(order.id())).build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Response get(@PathParam("id") long orderId) {
    Order order = orders.byId(orderId);
    if (order == null)
      return Response.status(404).build();
    return Response.ok(Mappers.toXmlOrder(order, Details.WITH_RELATED_ENTITIES)).build();
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
      if (cpa.signum() != 1 || cpa.compareTo(order.account().currentState().balance()) == 1)
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
