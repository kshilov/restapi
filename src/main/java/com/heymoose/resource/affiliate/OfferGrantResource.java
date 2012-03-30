package com.heymoose.resource.affiliate;

import static com.heymoose.util.WebAppUtil.checkNotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.heymoose.domain.User;
import com.heymoose.domain.UserRepository;
import com.heymoose.domain.affiliate.NewOffer;
import com.heymoose.domain.affiliate.NewOfferRepository;
import com.heymoose.domain.affiliate.NewOfferRepository.Ordering;
import com.heymoose.domain.affiliate.OfferGrant;
import com.heymoose.domain.affiliate.OfferGrantRepository;
import com.heymoose.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlOfferGrants;

@Path("grants")
@Singleton
public class OfferGrantResource {
  
  private final NewOfferRepository newOffers;
  private final OfferGrantRepository offerGrants;
  private final UserRepository users;
  
  @Inject
  public OfferGrantResource(NewOfferRepository newOffers,
                          OfferGrantRepository offerGrants,
                          UserRepository users) {
    this.newOffers = newOffers;
    this.offerGrants = offerGrants;
    this.users = users;
  }
  
  @GET
  @Transactional
  public XmlOfferGrants list(@QueryParam("offset") @DefaultValue("0") int offset,
                             @QueryParam("limit") @DefaultValue("20") int limit,
                             @QueryParam("ord") @DefaultValue("ID") Ordering ord,
                             @QueryParam("asc") @DefaultValue("false") boolean asc,
                             @QueryParam("offer_id") Long offerId,
                             @QueryParam("affiliate_id") Long affiliateId,
                             @QueryParam("approved") Boolean approved,
                             @QueryParam("active") Boolean active,
                             @QueryParam("full") @DefaultValue("false") boolean full) {
    return Mappers.toXmlOfferGrants(
        offerGrants.list(ord, asc, offset, limit, offerId, affiliateId, approved, active),
        offerGrants.count(offerId, affiliateId, approved, active),
        full
    );
  }
  
  @POST
  @Transactional
  public String create(@FormParam("offer_id") long offerId,
                       @FormParam("aff_id") long affiliateId,
                       @FormParam("message") String message) {
    checkNotNull(message);
    NewOffer offer = existingOffer(offerId);
    User affiliate = existingAffiliate(affiliateId);
    
    if (offerGrants.byOfferAndAffiliate(offerId, affiliateId) != null)
      throw new WebApplicationException(409);
    
    OfferGrant grant = new OfferGrant(offer.id(), affiliate.id(), message);
    offerGrants.put(grant);
    return grant.id().toString();
  }
  
  @PUT
  @Path("{id}/approved")
  @Transactional
  public Response approve(@PathParam("id") long id) {
    OfferGrant grant = existing(id);
    grant.moderateAsAdmin();
    return Response.ok().build();
  }
  
  @PUT
  @Path("{id}/active")
  @Transactional
  public Response activate(@PathParam("id") long id) {
    OfferGrant grant = existing(id);
    if (!grant.approved())
      throw new WebApplicationException(409);
    grant.moderateAsAdvertiser();
    return Response.ok().build();
  }
  
  private OfferGrant existing(long id) {
    OfferGrant grant = offerGrants.byId(id);
    if (grant == null)
      throw new WebApplicationException(404);
    return grant;
  }
  
  private NewOffer existingOffer(long id) {
    NewOffer offer = newOffers.byId(id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }
  
  private User existingUser(long id) {
    User user = users.byId(id);
    if (user == null)
      throw new WebApplicationException(404);
    return user;
  }
  
  private User existingAffiliate(long id) {
    User user = existingUser(id);
    if (!user.isAffiliate())
      throw new WebApplicationException(400);
    return user;
  }

}
