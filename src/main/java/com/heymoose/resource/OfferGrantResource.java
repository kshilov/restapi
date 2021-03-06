package com.heymoose.resource;

import com.heymoose.domain.user.User;
import com.heymoose.domain.user.UserRepository;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantFilter;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.grant.OfferGrantState;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.offer.OfferRepository.Ordering;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlOfferGrant;
import com.heymoose.resource.xml.XmlOfferGrants;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;

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
import java.net.URI;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;


@Path("grants")
@Singleton
public class OfferGrantResource {

  private final OfferRepository offers;
  private final OfferGrantRepository offerGrants;
  private final UserRepository users;

  @Inject
  public OfferGrantResource(OfferRepository offers,
                            OfferGrantRepository offerGrants,
                            UserRepository users) {
    this.offers = offers;
    this.offerGrants = offerGrants;
    this.users = users;
  }

  @GET
  @Transactional
  public XmlOfferGrants list(@QueryParam("offset") @DefaultValue("0") int offset,
                             @QueryParam("limit") @DefaultValue("20") int limit,
                             @QueryParam("ordering") @DefaultValue("ID")
                             Ordering ordering,
                             @QueryParam("direction") @DefaultValue("DESC")
                             OrderingDirection direction,
                             @QueryParam("offer_id") Long offerId,
                             @QueryParam("affiliate_id") Long affiliateId,
                             @QueryParam("state") OfferGrantState state,
                             @QueryParam("blocked") Boolean blocked,
                             @QueryParam("moderation") Boolean moderation,
                             @QueryParam("full") @DefaultValue("false") boolean full) {
    OfferGrantFilter filter = new OfferGrantFilter()
        .setOfferId(offerId)
        .setAffiliateId(affiliateId)
        .setState(state)
        .setModeration(moderation)
        .setBlocked(blocked);
    return Mappers.toXmlOfferGrants(
        offerGrants.list(ordering, direction, offset, limit, filter),
        offerGrants.count(filter), full
    );
  }

  @GET
  @Path("{id}")
  @Transactional
  public XmlOfferGrant get(@PathParam("id") long id,
                           @QueryParam("full") @DefaultValue("false") boolean full) {
    OfferGrant grant = existing(id);
    return Mappers.toXmlOfferGrant(grant, full);
  }

  @POST
  @Transactional
  public String create(@FormParam("offer_id") long offerId,
                       @FormParam("aff_id") long affiliateId,
                       @FormParam("message") String message,
                       @FormParam("postback_url") String postbackUrl) {
    Offer offer = visibleOffer(offerId);
    User affiliate = activeAffiliate(affiliateId);

    if (offerGrants.byOfferAndAffiliate(offerId, affiliateId) != null)
      throw new WebApplicationException(409);

    OfferGrant grant = new OfferGrant(offer.id(), affiliate.id(), message);
    if (postbackUrl != null) grant.setPostbackUrl(URI.create(postbackUrl));
    offerGrants.put(grant);
    return grant.id().toString();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public void update(@Context HttpContext context, @PathParam("id") long id) {
    OfferGrant grant = existing(id);
    Form form = context.getRequest().getEntity(Form.class);

    if (form.containsKey("back_url"))
      grant.setBackUrl(URI.create(form.getFirst("back_url")));
    if (form.containsKey("postback_url"))
      grant.setPostbackUrl(URI.create(form.getFirst("postback_url")));
  }

  @PUT
  @Path("{id}/approved")
  @Transactional
  public Response approve(@PathParam("id") long id) {
    OfferGrant grant = existing(id);
    if (grant.blocked())
      throw new WebApplicationException(409);
    grant.approve();
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}/approved")
  @Transactional
  public Response reject(@PathParam("id") long id, @FormParam("reason") String reason) {
    checkNotNull(reason);
    OfferGrant grant = existing(id);
    if (grant.blocked())
      throw new WebApplicationException(409);
    grant.reject(reason);
    return Response.ok().build();
  }

  @PUT
  @Path("{id}/blocked")
  @Transactional
  public Response block(@PathParam("id") long id, @FormParam("reason") String reason) {
    OfferGrant grant = existing(id);
    grant.block(reason);
    return Response.ok().build();
  }

  @DELETE
  @Path("{id}/blocked")
  @Transactional
  public Response unblock(@PathParam("id") long id) {
    OfferGrant grant = existing(id);
    grant.unblock();
    return Response.ok().build();
  }

  private OfferGrant existing(long id) {
    OfferGrant grant = offerGrants.byId(id);
    if (grant == null)
      throw new WebApplicationException(404);
    return grant;
  }

  private Offer visibleOffer(long id) {
    Offer offer = offers.byId(id);
    if (offer == null)
      throw new WebApplicationException(404);
    if (!offer.visible())
      throw new WebApplicationException(409);
    return offer;
  }

  private User existingUser(long id) {
    User user = users.byId(id);
    if (user == null)
      throw new WebApplicationException(404);
    return user;
  }

  private User activeAffiliate(long id) {
    User user = existingUser(id);
    if (!user.isAffiliate() || !user.active())
      throw new WebApplicationException(400);
    return user;
  }
}
