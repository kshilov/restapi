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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import com.heymoose.domain.Accounts;
import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.affiliate.NewOffer;
import com.heymoose.domain.affiliate.NewOfferRepository.Ordering;
import com.heymoose.domain.affiliate.CpaPolicy;
import com.heymoose.domain.affiliate.NewOfferRepository;
import com.heymoose.domain.affiliate.PayMethod;
import com.heymoose.domain.affiliate.Region;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlNewOffers;
import static com.heymoose.util.WebAppUtil.checkNotNull;

@Path("offers")
@Singleton
public class NewOfferResource {
  
  private final NewOfferRepository newOffers;
  private final UserRepository users;
  private final Accounts accounts;
  
  @Inject
  public NewOfferResource(NewOfferRepository newOffers,
                          UserRepository users,
                          Accounts accounts) {
    this.newOffers = newOffers;
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
  
  @POST
  @Transactional
  public String create(@FormParam("advertiser_id") Long advertiserId,
                       @FormParam("pay_method") PayMethod payMethod,
                       @FormParam("cpa_policy") CpaPolicy cpaPolicy,
                       @FormParam("cost") String strCost,
                       @FormParam("balance") String strBalance,
                       @FormParam("name") String name,
                       @FormParam("url") String url,
                       @FormParam("title") String title,
                       @FormParam("allow_negative_balance") @DefaultValue("false") boolean allowNegativeBalance,
                       @FormParam("auto_approve") @DefaultValue("false") boolean autoApprove,
                       @FormParam("reentrant") @DefaultValue("true") boolean reentrant,
                       @FormParam("regions") List<Region> regions) {
    checkNotNull(advertiserId, payMethod, strCost, name, url, title);
    if (payMethod == PayMethod.CPA)
      checkNotNull(cpaPolicy);
    
    User advertiser = users.byId(advertiserId);
    if (advertiser == null)
      throw new WebApplicationException(404);
    
    BigDecimal cost = new BigDecimal(strCost), percent = null;
    BigDecimal balance = new BigDecimal(strBalance);
    if (cost.signum() != 1 || balance.signum() < 0)
      throw new WebApplicationException(400);
    
    if (payMethod == PayMethod.CPA && cpaPolicy == CpaPolicy.PERCENT) {
      percent = cost;
      cost = null;
    }
    
    NewOffer offer = new NewOffer(advertiser, allowNegativeBalance, name, payMethod, 
        cpaPolicy, cost, percent, title, url, autoApprove, reentrant, regions);
    
    if (balance.signum() > 0) {
      if (advertiser.customerAccount().getBalance().compareTo(balance) == -1)
        throw new WebApplicationException(409);
      accounts.transfer(advertiser.customerAccount(), offer.account(), balance);
    }
    
    newOffers.put(offer);
    return offer.id().toString();
  }
}
