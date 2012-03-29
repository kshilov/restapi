package com.heymoose.resource.affiliate;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import com.google.common.collect.Lists;
import com.heymoose.domain.Accounts;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.affiliate.NewOffer;
import com.heymoose.domain.affiliate.NewOfferRepository.Ordering;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.NewOfferRepository;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.domain.affiliate.SubOffer;
import com.heymoose.domain.affiliate.SubOfferRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlNewOffer;
import com.heymoose.resource.xml.XmlNewOffers;
import com.heymoose.resource.xml.XmlSubOffers;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

@Path("offers")
@Singleton
public class NewOfferResource {
  
  private final NewOfferRepository newOffers;
  private final SubOfferRepository subOffers;
  private final UserRepository users;
  private final Accounts accounts;
  
  @Inject
  public NewOfferResource(NewOfferRepository newOffers,
                          SubOfferRepository subOffers,
                          UserRepository users,
                          Accounts accounts) {
    this.newOffers = newOffers;
    this.subOffers = subOffers;
    this.users = users;
    this.accounts = accounts;
  }
  
  @GET
  @Transactional
  public XmlNewOffers list(@QueryParam("offset") @DefaultValue("0") int offset,
                           @QueryParam("limit") @DefaultValue("20") int limit,
                           @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                           @QueryParam("asc") @DefaultValue("false") boolean asc,
                           @QueryParam("advertiser_id") Long advertiserId) {
    return Mappers.toXmlNewOffers(
        newOffers.list(ord, asc, offset, limit, advertiserId),
        newOffers.count(advertiserId)
    );
  }
  
  @GET
  @Path("{id}")
  @Transactional
  public XmlNewOffer get(@PathParam("id") long offerId) {
    return Mappers.toXmlNewOffer(existing(offerId));
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
                       @FormParam("regions") List<String> strRegions) {
    checkNotNull(advertiserId, payMethod, strCost, name, description, url, title);
    checkArgument(!strRegions.isEmpty());
    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);
    
    User advertiser = users.byId(advertiserId);
    if (advertiser == null)
      throw new WebApplicationException(404);
    
    BigDecimal cost = new BigDecimal(strCost), percent = null;
    BigDecimal balance = new BigDecimal(strBalance);
    if (cost.signum() != 1 || balance.signum() < 0)
      throw new WebApplicationException(400);
    if (balance.signum() > 0 && advertiser.customerAccount().getBalance().compareTo(balance) == -1)
      throw new WebApplicationException(409);
    
    if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.PERCENT) {
      percent = cost;
      cost = null;
    }
    
    List<Region> regions = Lists.newArrayList();
    for (String strRegion : strRegions)
      regions.add(Region.valueOf(strRegion));
    
    NewOffer offer = new NewOffer(advertiser, allowNegativeBalance, name, description,
        payMethod, cpaPolicy, cost, percent, title, url, autoApprove, reentrant, regions,
        logoFileName);
    newOffers.put(offer);

    if (balance.signum() > 0)
      accounts.transfer(advertiser.customerAccount(), offer.account(), balance);
    
    return offer.id().toString();
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
                               @FormParam("reentrant") @DefaultValue("true") boolean reentrant) {
    checkNotNull(cpaPolicy, strCost, title);
    NewOffer offer = existing(offerId);
    
    BigDecimal cost = new BigDecimal(strCost), percent = null;
    if (cpaPolicy == CpaPolicy.PERCENT) {
      percent = cost;
      cost = null;
    }
    
    SubOffer suboffer = new SubOffer(offer.id(), cpaPolicy, cost, percent, 
                                     title, autoApprove, reentrant);
    subOffers.put(suboffer);
    return suboffer.id().toString();
  }
  
  private NewOffer existing(long id) {
    NewOffer offer = newOffers.byId(id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }
}
