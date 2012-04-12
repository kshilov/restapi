package com.heymoose.resource.affiliate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import com.heymoose.domain.Banner;
import com.heymoose.domain.BannerStore;
import com.heymoose.domain.Offer;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.Category;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.domain.affiliate.OfferRepository;
import com.heymoose.domain.affiliate.OfferRepository.Ordering;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.affiliate.SubOfferRepository;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.badRequest;
import static com.heymoose.resource.Exceptions.conflict;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlOffers;
import com.heymoose.resource.xml.XmlSubOffers;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
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

@Path("offers")
@Singleton
public class OfferResource {
  
  private final OfferRepository offers;
  private final SubOfferRepository subOffers;
  private final OfferGrantRepository offerGrants;
  private final UserRepository users;
  private final Accounting accounting;
  private final Repo repo;
  private final BannerStore bannerStore;

  @Inject
  public OfferResource(OfferRepository offers,
                       SubOfferRepository subOffers,
                       OfferGrantRepository offerGrants,
                       UserRepository users, Accounting accounting, Repo repo, BannerStore bannerStore) {
    this.offers = offers;
    this.subOffers = subOffers;
    this.offerGrants = offerGrants;
    this.users = users;
    this.accounting = accounting;
    this.repo = repo;
    this.bannerStore = bannerStore;
  }
  
  @GET
  @Transactional
  public XmlOffers list(@QueryParam("offset") @DefaultValue("0") int offset,
                           @QueryParam("limit") @DefaultValue("20") int limit,
                           @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                           @QueryParam("asc") @DefaultValue("false") boolean asc,
                           @QueryParam("approved") Boolean approved,
                           @QueryParam("active") Boolean active,
                           @QueryParam("advertiser_id") Long advertiserId,
                           @QueryParam("aff_id") Long affiliateId) {
    Iterable<Offer> offers = this.offers.list(ord, asc, offset, limit,
        approved, active, advertiserId);
    long count = this.offers.count(approved, active, advertiserId);
    if (affiliateId != null && count > 0) {
      List<Long> offerIds = newArrayList();
      for (Offer offer : offers)
        offerIds.add(offer.id());
      Map<Long, OfferGrant> grants = offerGrants.byOffersAndAffiliate(offerIds, affiliateId);
      return Mappers.toXmlOffers(offers, grants, count);
    }
    else
      return Mappers.toXmlOffers(offers, count);
  }
  
  @GET
  @Path("requested")
  @Transactional
  public XmlOffers listRequested(@QueryParam("offset") @DefaultValue("0") int offset,
                                    @QueryParam("limit") @DefaultValue("20") int limit,
                                    @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                                    @QueryParam("asc") @DefaultValue("false") boolean asc,
                                    @QueryParam("aff_id") long affiliateId,
                                    @QueryParam("active") Boolean active) {
    return Mappers.toXmlGrantedOffers(
        offerGrants.list(ord, asc, offset, limit, null, affiliateId, null, active),
        offerGrants.count(null, affiliateId, null, active)
    );
  }
  
  @GET
  @Path("{id}")
  @Transactional
  public XmlOffer get(@PathParam("id") long offerId,
                         @QueryParam("approved") @DefaultValue("false") boolean approved,
                         @QueryParam("active") @DefaultValue("false") boolean active) {
    Offer offer = existing(offerId);
    if (approved && !offer.approved() || active && !offer.active())
      throw new WebApplicationException(403);
    return Mappers.toXmlOffer(offer);
  }
  
  @GET
  @Path("{id}/requested")
  @Transactional
  public XmlOffer getRequested(@PathParam("id") long offerId,
                                  @QueryParam("aff_id") long affiliateId) {
    OfferGrant grant = existingGrant(offerId, affiliateId);
    return Mappers.toXmlGrantedNewOffer(grant);
  }
  
  @POST
  @Transactional
  public String create(@FormParam("advertiser_id") Long advertiserId,
                       @FormParam("pay_method") PayMethod payMethod,
                       @FormParam("cpa_policy") CpaPolicy cpaPolicy,
                       @FormParam("cost") String strCost,
                       @FormParam("balance") String strBalance,
                       @FormParam("name") String name,
                       @FormParam("description") String description,
                       @FormParam("logo_filename") String logoFileName,
                       @FormParam("url") String url,
                       @FormParam("title") String title,
                       @FormParam("allow_negative_balance") @DefaultValue("false") boolean allowNegativeBalance,
                       @FormParam("auto_approve") @DefaultValue("false") boolean autoApprove,
                       @FormParam("reentrant") @DefaultValue("true") boolean reentrant,
                       @FormParam("regions") List<String> strRegions,
                       @FormParam("categories") List<Long> longCategories,
                       @FormParam("code") String code,
                       @FormParam("hold_days") Integer holdDays,
                       @FormParam("cookie_ttl") Integer cookieTtl) {
    checkNotNull(advertiserId, payMethod, strCost, name, description, url, title, code, holdDays, cookieTtl);
    checkNotNull(URI.create(url));
    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);
    
    User advertiser = activeAdvertiser(advertiserId);
    
    BigDecimal cost = new BigDecimal(strCost), percent = null;
    BigDecimal balance = new BigDecimal(strBalance);
    if (cost.signum() != 1 || balance.signum() < 0)
      throw new WebApplicationException(400);
    if (balance.signum() > 0 && advertiser.advertiserAccount().balance().compareTo(balance) == -1)
      throw new WebApplicationException(409);
    
    if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.PERCENT) {
      percent = cost;
      cost = null;
    }
    
    List<Region> regions = newArrayList();
    for (String strRegion : strRegions)
      regions.add(Region.valueOf(strRegion));

    if (longCategories == null)
      longCategories = newArrayList();
    Iterable<Category> categories = repo.get(Category.class, newHashSet(longCategories)).values();

    checkCode(code, advertiser.id(), null);

    Offer offer = new Offer(advertiser, allowNegativeBalance, name, description,
        payMethod, cpaPolicy, cost, percent, title, url, autoApprove, reentrant, regions, categories,
        logoFileName, code, holdDays, cookieTtl);
    offers.put(offer);

    if (balance.signum() > 0)
      accounting.transferMoney(advertiser.advertiserAccount(), offer.account(), balance, AccountingEvent.OFFER_ACCOUNT_ADD, offer.id());
    
    return offer.id().toString();
  }
  
  @PUT
  @Path("{id}")
  @Transactional
  public void update(@Context HttpContext context, @PathParam("id") long id) {
    Offer offer = existing(id);
    Form form = context.getRequest().getEntity(Form.class);
    
    if (form.containsKey("pay_method"))
      offer.setPayMethod(PayMethod.valueOf(form.getFirst("pay_method")));
    if (form.containsKey("cpa_policy"))
      offer.setCpaPolicy(CpaPolicy.valueOf(form.getFirst("cpa_policy")));
    if (form.containsKey("cost")) {
      BigDecimal value = new BigDecimal(form.getFirst("cost"));
      if (offer.payMethod() == PayMethod.CPA && offer.cpaPolicy() == CpaPolicy.PERCENT)
        offer.setPercent(value);
      else
        offer.setCost(value);
    }
    if (form.containsKey("title"))
      offer.setTitle(form.getFirst("title"));
    if (form.containsKey("code"))
      offer.setCode(form.getFirst("code"));
    if (form.containsKey("hold_days"))
      offer.setHoldDays(Integer.parseInt(form.getFirst("hold_days")));
    if (form.containsKey("auto_approve"))
      offer.setAutoApprove(Boolean.parseBoolean(form.getFirst("auto_approve")));
    if (form.containsKey("reentrant"))
      offer.setReentrant(Boolean.parseBoolean(form.getFirst("reentrant")));
    
    if (form.containsKey("name"))
      offer.setName(form.getFirst("name"));
    if (form.containsKey("description"))
      offer.setDescription(form.getFirst("description"));
    if (form.containsKey("url"))
      offer.setUrl(URI.create(form.getFirst("url")));
    if (form.containsKey("cookie_ttl"))
      offer.setCookieTtl(Integer.parseInt(form.getFirst("cookie_ttl")));
    if (form.containsKey("categories")) {
      List<Long> categIds = newArrayList();
      for (String strId : form.get("categories"))
        categIds.add(Long.parseLong(strId));
      Iterable<Category> categories = repo.get(Category.class, newHashSet(categIds)).values();
      offer.setCategories(categories);
    }
    if (form.containsKey("regions")) {
      List<Region> regions = newArrayList();
      for (String strRegion : form.get("regions"))
        regions.add(Region.valueOf(strRegion));
      offer.setRegions(regions);
    }
    if (form.containsKey("allow_negative_balance"))
      offer.account().setAllowNegativeBalance(Boolean.parseBoolean(form.getFirst("allow_negative_balance")));
    if (form.containsKey("auto_approve"))
      offer.setAutoApprove(Boolean.parseBoolean(form.getFirst("auto_approve")));
    if (form.containsKey("reentrant"))
      offer.setReentrant(Boolean.parseBoolean(form.getFirst("reentrant")));
    if (form.containsKey("logo_filename"))
      offer.setLogoFileName(form.getFirst("logo_filename"));
    if (form.containsKey("token_param_name"))
      offer.setTokenParamName(form.getFirst("token_param_name"));
  }

  @PUT
  @Path("code")
  @Transactional
  public void checkCode(@FormParam("code") String code,
                        @FormParam("advertiser_id") Long advertiserId,
                        @FormParam("offer_id") Long offerId) {
    checkNotNull(code, advertiserId);
    
    SubOffer existentSub = repo.byHQL(
        SubOffer.class,
        "from SubOffer o where o.code = ? and o.parent.advertiser.id = ?",
        code, advertiserId
    );

    if (existentSub != null && existentSub.id() != offerId)
      throw conflict();

    Offer existentOffer = repo.byHQL(
        Offer.class,
        "from Offer o where o.code = ? and o.advertiser.id = ?",
        code, advertiserId
    );

    if (existentOffer != null && existentOffer.id() != offerId)
      throw conflict();
  }

  @PUT
  @Path("{id}/blocked")
  @Transactional
  public Response block(@PathParam("id") long id, @FormParam("reason") String reason) {
    Offer offer = existing(id);
    offer.block(reason);
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}/blocked")
  @Transactional
  public Response unblock(@PathParam("id") long id) {
    Offer offer = existing(id);
    offer.unblock();
    return Response.ok().build();
  }
  
  @GET
  @Path("{id}/suboffers")
  @Transactional
  public XmlSubOffers listSuboffers(@PathParam("id") long parentId) {
    return Mappers.toXmlSubOffers(
        subOffers.list(parentId),
        subOffers.count(parentId)
    );
  }
  
  @POST
  @Path("{id}/suboffers")
  @Transactional
  public String createSuboffer(@PathParam("id") long offerId,
                               @FormParam("cpa_policy") CpaPolicy cpaPolicy,
                               @FormParam("cost") String strCost,
                               @FormParam("title") String title,
                               @FormParam("auto_approve") @DefaultValue("false") boolean autoApprove,
                               @FormParam("reentrant") @DefaultValue("true") boolean reentrant,
                               @FormParam("code") String code,
                               @FormParam("hold_days") Integer holdDays) {
    checkNotNull(cpaPolicy, strCost, title, code, holdDays);
    Offer offer = existing(offerId);
    
    BigDecimal cost = new BigDecimal(strCost), percent = null;
    if (cpaPolicy == CpaPolicy.PERCENT) {
      percent = cost;
      cost = null;
    }

    checkCode(code, offer.advertiser().id(), null);

    SubOffer suboffer = new SubOffer(offer.id(), cpaPolicy, cost, percent,
                                     title, autoApprove, reentrant, code, holdDays);
    subOffers.put(suboffer);
    return suboffer.id().toString();
  }
  
  @PUT
  @Path("{id}/suboffers/{subofferId}")
  @Transactional
  public void updateSuboffer(@Context HttpContext context,
                             @PathParam("id") long offerId, @PathParam("subofferId") long subofferId) {
    Offer offer = existing(offerId);
    SubOffer suboffer = null;
    for (SubOffer sub : offer.suboffers())
      if (sub.id().equals(subofferId)) {
        suboffer = sub;
        break;
      }
    if (suboffer == null)
      throw badRequest();
    
    Form form = context.getRequest().getEntity(Form.class);
    if (form.containsKey("cpa_policy"))
      suboffer.setCpaPolicy(CpaPolicy.valueOf(form.getFirst("cpa_policy")));
    if (form.containsKey("cost")) {
      BigDecimal value = new BigDecimal(form.getFirst("cost"));
      if (suboffer.cpaPolicy() == CpaPolicy.PERCENT)
        suboffer.setPercent(value);
      else
        suboffer.setCost(value);
    }
    if (form.containsKey("title"))
      suboffer.setTitle(form.getFirst("title"));
    if (form.containsKey("code"))
      suboffer.setCode(form.getFirst("code"));
    if (form.containsKey("hold_days"))
      suboffer.setHoldDays(Integer.parseInt(form.getFirst("hold_days")));
    if (form.containsKey("auto_approve"))
      suboffer.setAutoApprove(Boolean.parseBoolean(form.getFirst("auto_approve")));
    if (form.containsKey("reentrant"))
      suboffer.setReentrant(Boolean.parseBoolean(form.getFirst("reentrant")));
    if (form.containsKey("active"))
      suboffer.setActive(Boolean.parseBoolean(form.getFirst("active")));
  }

  @POST
  @Path("{id}/banners")
  @Transactional
  public String addBanner(@PathParam("id") long offerId,
                        @FormParam("width") Integer width,
                        @FormParam("height") Integer height,
                        @FormParam("mime_type") String mimeType,
                        @FormParam("image") String image) throws IOException {
    checkNotNull(mimeType, image);
    Offer offer = existing(offerId);
    Banner banner = new Banner(offer, mimeType, width, height);
    offer.addBanner(banner);
    repo.put(banner);
    bannerStore.saveBanner(banner.id(), image);
    return banner.id().toString();
  }

  @DELETE
  @Path("{id}/banners/{bannerId}")
  @Transactional
  public void deleteBanner(@PathParam("id") long offerId, @PathParam("bannerId") long bannerId) {
    Offer offer = existing(offerId);
    boolean found = false;
    for (Banner banner : offer.banners()) {
      if (banner.id().equals(bannerId)) {
        found = true;
        offer.deleteBanner(banner);
        break;
      }
    }
    if (!found)
      throw badRequest();
  }
  
  private Offer existing(long id) {
    Offer offer = offers.byId(id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }
  
  private OfferGrant existingGrant(long offerId, long affiliateId) {
    OfferGrant grant = offerGrants.byOfferAndAffiliate(offerId, affiliateId);
    if (grant == null)
      throw new WebApplicationException(404);
    return grant;
  }
  
  private User existingUser(long id) {
    User user = users.byId(id);
    if (user == null)
      throw new WebApplicationException(404);
    return user;
  }
  
  private User activeAdvertiser(long id) {
    User user = existingUser(id);
    if (!user.isAdvertiser() || !user.active())
      throw new WebApplicationException(400);
    return user;
  }
}
