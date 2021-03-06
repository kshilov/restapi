package com.heymoose.resource;

import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Banner;
import com.heymoose.domain.offer.Category;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferFilter;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.OfferRepository.Ordering;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.offer.SubOfferRepository;
import com.heymoose.domain.settings.Settings;
import com.heymoose.domain.user.User;
import com.heymoose.domain.user.UserRepository;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.BannerStore;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlOffer;
import com.heymoose.resource.xml.XmlOffers;
import com.heymoose.resource.xml.XmlSubOffers;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;
import static com.heymoose.resource.Exceptions.*;

@Path("offers")
@Singleton
public class OfferResource {

  private final OfferRepository offers;
  private final SubOfferRepository subOffers;
  private final Sites sites;
  private final UserRepository users;
  private final Accounting accounting;
  private final Repo repo;
  private final BannerStore bannerStore;
  private final Settings settings;

  @Inject
  public OfferResource(OfferRepository offers, SubOfferRepository subOffers, Sites sites,
                       UserRepository users, Accounting accounting, Repo repo, BannerStore bannerStore,
                       Settings settings) {
    this.offers = offers;
    this.subOffers = subOffers;
    this.sites = sites;
    this.users = users;
    this.accounting = accounting;
    this.repo = repo;
    this.bannerStore = bannerStore;
    this.settings = settings;
  }

  @GET
  @Transactional
  public XmlOffers list(@QueryParam("offset") @DefaultValue("0") int offset,
                        @QueryParam("limit") @DefaultValue("20") int limit,
                        @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                        @QueryParam("asc") @DefaultValue("false") boolean asc,
                        @QueryParam("approved") Boolean approved,
                        @QueryParam("active") Boolean active,
                        @QueryParam("launched") Boolean launched,
                        @QueryParam("showcase") Boolean showcase,
                        @QueryParam("exclusive") Boolean exclusiveOnly,
                        @QueryParam("advertiser_id") Long advertiserId,
                        @QueryParam("aff_id") Long affiliateId,
                        @QueryParam("pay_method") String payMethod,
                        @QueryParam("cpa_policy") String cpaPolicy,
                        @QueryParam("region") List<String> regionList,
                        @QueryParam("category") List<Long> categoryList) {
    OfferFilter filter = new OfferFilter()
        .setActive(active)
        .setAdvertiserId(advertiserId)
        .setAffiliateId(affiliateId)
        .setApproved(approved)
        .setLaunched(launched)
        .setShowcase(showcase)
        .setExclusive(exclusiveOnly)
        .setRegionList(regionList)
        .setCategoryList(categoryList);

    if (payMethod != null)
      filter.setPayMethod(PayMethod.valueOf(payMethod.toUpperCase()));

    if (cpaPolicy != null)
      filter.setCpaPolicy(CpaPolicy.valueOf(cpaPolicy.toUpperCase()));

    Iterable<Offer> offers = this.offers.list(ord, asc, offset, limit, filter);
    long count = this.offers.count(filter);
    if (affiliateId != null && count > 0) {
      XmlOffers xmlOffers = new XmlOffers();
      xmlOffers.count = count;
      for (Offer offer: offers) {
        Long offerSiteCount = sites.placementCount(offer.id(), affiliateId);
        xmlOffers.offers.add(Mappers.toXmlOffer(offer, offerSiteCount));
      }
      return xmlOffers;
    } else {
      return Mappers.toXmlOffers(offers, count);
    }
  }

  @GET
  @Path("requested")
  @Transactional
  public XmlOffers listRequested(@QueryParam("offset") @DefaultValue("0") int offset,
                                 @QueryParam("limit") @DefaultValue("20") int limit,
                                 @QueryParam("aff_id") long affiliateId) {
    Pair<List<Offer>, Long> result =
        offers.affiliateOfferList(affiliateId, offset, limit);
    return Mappers.toXmlOffers(result.fst, result.snd);
  }

  @GET
  @Path("{id}")
  @Transactional
  public XmlOffer get(@PathParam("id") long offerId,
                      @QueryParam("aff_id") Long affId,
                      @QueryParam("approved") @DefaultValue("false") boolean approved,
                      @QueryParam("active") @DefaultValue("false") boolean active) {
    Offer offer = existing(offerId);
    if (approved && !offer.approved() || active && !offer.active())
      throw new WebApplicationException(403);
    Long placementsCount = null;
    if (affId != null)
      placementsCount = sites.placementCount(offerId, affId);
    return Mappers.toXmlOffer(offer, placementsCount);
  }

  @GET
  @Path("referral")
  @Transactional
  public XmlOffer referralOffer() {
    Long referralOfferId = settings.getLongOrNull(Settings.REFERRAL_OFFER);
    return get(referralOfferId, null, true, true);
  }

  @POST
  @Transactional
  public String create(@FormParam("advertiser_id") Long advertiserId,
                       @FormParam("pay_method") PayMethod payMethod,
                       @FormParam("cpa_policy") CpaPolicy cpaPolicy,
                       @FormParam("cost") String strCost,
                       @FormParam("cost2") String strCost2,
                       @FormParam("percent") String strPercent,
                       @FormParam("balance") String strBalance,
                       @FormParam("name") String name,
                       @FormParam("description") String description,
                       @FormParam("short_description") String shortDescription,
                       @FormParam("logo_filename") String logoFileName,
                       @FormParam("url") String url,
                       @FormParam("site_url") String siteUrl,
                       @FormParam("title") String title,
                       @FormParam("allow_negative_balance") @DefaultValue("false") boolean allowNegativeBalance,
                       @FormParam("auto_approve") @DefaultValue("false") boolean autoApprove,
                       @FormParam("reentrant") @DefaultValue("true") boolean reentrant,
                       @FormParam("regions") List<String> strRegions,
                       @FormParam("categories") List<Long> longCategories,
                       @FormParam("code") String code,
                       @FormParam("hold_days") Integer holdDays,
                       @FormParam("cookie_ttl") Integer cookieTtl,
                       @FormParam("launch_time") Long unixLaunchTime,
                       @FormParam("allow_deeplink") @DefaultValue("false") boolean allowDeeplink,
                       @FormParam("yml_url") String ymlUrl,
                       @FormParam("allow_cashback") @DefaultValue("false")
                       boolean allowCashback) {
    checkNotNull(advertiserId, payMethod, name, description, shortDescription, url, siteUrl,
        title, code, holdDays, cookieTtl, unixLaunchTime);
    checkNotNull(URI.create(url));
    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);

    User advertiser = activeAdvertiser(advertiserId);

    BigDecimal balance = new BigDecimal(strBalance);
    if (balance.signum() < 0)
      throw new WebApplicationException(400);
    if (balance.signum() > 0 && advertiser.advertiserAccount().balance().compareTo(balance) == -1)
      throw new WebApplicationException(409);

    BigDecimal cost = null, cost2 = null, percent = null;
    if (payMethod == PayMethod.CPC)
      cost = positiveDecimal(strCost);
    else if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.FIXED)
      cost = positiveDecimal(strCost);
    else if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.PERCENT)
      percent = positiveDecimal(strPercent);
    else if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.DOUBLE_FIXED) {
      cost = positiveDecimal(strCost);
      cost2 = positiveDecimal(strCost2);
    } else
      throw new WebApplicationException(400);

    List<String> regions = newArrayList();
    for (String strRegion : strRegions)
      regions.add(strRegion);

    if (longCategories == null)
      longCategories = newArrayList();
    Iterable<Category> categories = repo.get(Category.class, newHashSet(longCategories)).values();

    checkCode(code, advertiser.id(), null);
    DateTime launchTime = new DateTime(unixLaunchTime);

    Offer offer = new Offer(advertiser, allowNegativeBalance, name, description, shortDescription,
        payMethod, cpaPolicy, cost, cost2, percent, title, url, siteUrl, autoApprove, reentrant,
        regions, categories, logoFileName, code, holdDays, cookieTtl, launchTime, allowDeeplink);
    offer.setYmlUrl(ymlUrl)
        .setAllowCashback(allowCashback);
    offers.put(offer);

    if (balance.signum() > 0)
      accounting.addOfferFunds(offer, balance);

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

    PayMethod payMethod = offer.payMethod();
    CpaPolicy cpaPolicy = offer.cpaPolicy();

    if ((payMethod == PayMethod.CPC || payMethod == PayMethod.CPA &&
        (cpaPolicy == CpaPolicy.FIXED || cpaPolicy == CpaPolicy.DOUBLE_FIXED)) &&
        form.containsKey("cost"))
      offer.setCost(new BigDecimal(form.getFirst("cost")));
    if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.DOUBLE_FIXED && form.containsKey("cost2"))
      offer.setCost2(new BigDecimal(form.getFirst("cost2")));
    if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.PERCENT && form.containsKey("percent"))
      offer.setPercent(new BigDecimal(form.getFirst("percent")));
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
    if (form.containsKey("short_description"))
      offer.setShortDescription(form.getFirst("short_description"));
    if (form.containsKey("cr"))
      offer.setCr(new BigDecimal(form.getFirst("cr")));
    if (form.containsKey("showcase"))
      offer.setShowcase(Boolean.parseBoolean(form.getFirst("showcase")));
    if (form.containsKey("url"))
      offer.setUrl(URI.create(form.getFirst("url")));
    if (form.containsKey("site_url"))
      offer.setSiteUrl(URI.create(form.getFirst("site_url")));
    if (form.containsKey("cookie_ttl"))
      offer.setCookieTtl(Integer.parseInt(form.getFirst("cookie_ttl")));
    if (form.containsKey("launch_time"))
      offer.setLaunchTime(new DateTime(Long.parseLong(form.getFirst("launch_time"))));
    if (form.containsKey("allow_deeplink"))
      offer.setAllowDeeplink(Boolean.parseBoolean(form.getFirst("allow_deeplink")));
    if (form.containsKey("categories")) {
      List<Long> categIds = newArrayList();
      for (String strId : form.get("categories"))
        if (strId != null && !strId.isEmpty())
          categIds.add(Long.parseLong(strId));
      Iterable<Category> categories = repo.get(Category.class, newHashSet(categIds)).values();
      offer.setCategories(categories);
    }
    if (form.containsKey("regions")) {
      List<String> regions = newArrayList();
      for (String strRegion : form.get("regions"))
        if (strRegion != null && !strRegion.isEmpty())
          regions.add(strRegion);
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
    if (form.containsKey("yml_url"))
      offer.setYmlUrl(form.getFirst("yml_url"));
    if (form.containsKey("allow_cashback"))
      offer.setAllowCashback(Boolean.valueOf(form.getFirst("allow_cashback")));

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

    if (existentSub != null && !existentSub.id().equals(offerId))
      throw conflict();

    Offer existentOffer = repo.byHQL(
        Offer.class,
        "from Offer o where o.code = ? and o.advertiser.id = ?",
        code, advertiserId
    );

    if (existentOffer != null && !existentOffer.id().equals(offerId))
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
                               @FormParam("cost2") String strCost2,
                               @FormParam("percent") String strPercent,
                               @FormParam("title") String title,
                               @FormParam("auto_approve") @DefaultValue("false") boolean autoApprove,
                               @FormParam("reentrant") @DefaultValue("true") boolean reentrant,
                               @FormParam("code") String code,
                               @FormParam("hold_days") Integer holdDays) {
    checkNotNull(cpaPolicy, title, code, holdDays);
    Offer offer = existing(offerId);

    BigDecimal cost = null, cost2 = null, percent = null;
    if (cpaPolicy == CpaPolicy.FIXED)
      cost = positiveDecimal(strCost);
    else if (cpaPolicy == CpaPolicy.PERCENT)
      percent = positiveDecimal(strPercent);
    else if (cpaPolicy == CpaPolicy.DOUBLE_FIXED) {
      cost = positiveDecimal(strCost);
      cost2 = positiveDecimal(strCost2);
    } else
      throw new WebApplicationException(400);

    checkCode(code, offer.advertiser().id(), null);

    SubOffer suboffer = new SubOffer(offer.id(), cpaPolicy, cost, cost2, percent,
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

    CpaPolicy cpaPolicy = suboffer.cpaPolicy();
    if ((cpaPolicy == CpaPolicy.FIXED || cpaPolicy == CpaPolicy.DOUBLE_FIXED) &&
        form.containsKey("cost"))
      suboffer.setCost(new BigDecimal(form.getFirst("cost")));
    if (cpaPolicy == CpaPolicy.DOUBLE_FIXED && form.containsKey("cost2"))
      suboffer.setCost2(new BigDecimal(form.getFirst("cost2")));
    if (cpaPolicy == CpaPolicy.PERCENT && form.containsKey("percent"))
      suboffer.setPercent(new BigDecimal(form.getFirst("percent")));
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
                          @FormParam("url") String url,
                          @FormParam("image") String image) throws IOException {
    checkNotNull(mimeType, image);
    Offer offer = existing(offerId);
    Banner banner = new Banner(offer, mimeType, width, height, url);
    offer.addBanner(banner);
    repo.put(banner);
    bannerStore.saveBanner(banner.id(), image);
    return banner.id().toString();
  }

  @PUT
  @Path("{id}/banners/{bannerId}")
  @Transactional
  public void updateBanner(@Context HttpContext context,
                           @PathParam("id") long offerId, @PathParam("bannerId") long bannerId) {
    Offer offer = existing(offerId);
    Banner banner = existingBanner(offer, bannerId);
    Form form = context.getRequest().getEntity(Form.class);

    if (form.containsKey("url")) {
      String strUrl = form.getFirst("url");
      banner.setUrl((strUrl != null && !strUrl.isEmpty()) ? URI.create(strUrl).toString() : null);
    }
  }

  @DELETE
  @Path("{id}/banners/{bannerId}")
  @Transactional
  public void deleteBanner(@PathParam("id") long offerId, @PathParam("bannerId") long bannerId) {
    Offer offer = existing(offerId);
    offer.deleteBanner(existingBanner(offer, bannerId));
  }

  @PUT
  @Path("{id}/account")
  @Transactional
  public void addToBalance(@PathParam("id") long offerId, @FormParam("amount") double dAmount) {
    Offer offer = existing(offerId);
    BigDecimal amount = new BigDecimal(dAmount);
    if (offer.advertiser().advertiserAccount().balance().compareTo(amount) == -1)
      throw new WebApplicationException(409);
    accounting.addOfferFunds(offer, amount);
  }

  @DELETE
  @Path("{id}/account")
  @Transactional
  public void removeFromBalance(@PathParam("id") long offerId, @FormParam("amount") double dAmount) {
    Offer offer = existing(offerId);
    BigDecimal amount = new BigDecimal(dAmount);
    if (offer.account().balance().compareTo(amount) == -1)
      throw new WebApplicationException(409);
    accounting.transferMoney(offer.account(), offer.advertiser().advertiserAccount(), amount,
        AccountingEvent.OFFER_ACCOUNT_REMOVE, null);
  }

  private Offer existing(long id) {
    Offer offer = offers.byId(id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }

  private Banner existingBanner(Offer offer, long bannerId) {
    for (Banner banner : offer.banners())
      if (banner.id().equals(bannerId))
        return banner;
    throw badRequest();
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

  private BigDecimal positiveDecimal(String strDecimal) {
    BigDecimal decimal = new BigDecimal(strDecimal);
    if (decimal.signum() != 1)
      throw new WebApplicationException(400);
    return decimal;
  }
}
