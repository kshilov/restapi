package com.heymoose.resource;

import com.google.inject.Inject;
import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.Placement;
import com.heymoose.domain.site.Site;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.MapToXml;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResultToXml;
import com.heymoose.infrastructure.util.db.QueryResult;
import org.jdom2.Element;

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
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("placements")
public class PlacementResource {

  private static final MapToXml PLACEMENT_MAPPER = new MapToXml()
            .setElementName("placement")
            .addAttribute("id")
            .addChild("admin_state")
            .addChild("admin_comment")
            .addChild("creation_time")
            .addChild("last_change_time")
            .addChild("back_url")
            .addChild("postback_url")
            .addSubMapper(new MapToXml()
                .setElementName("offer")
                .addAttribute("id")
                .addChild("name"))
            .addSubMapper(new MapToXml()
                .setElementName("affiliate")
                .addAttribute("id")
                .addChild("email")
                .addChild("blocked"))
            .addSubMapper(new MapToXml()
                .setElementName("site")
                .addAttribute("id")
                .addChild("type")
                .addChild("name")
                .addChild("admin_state")
                .addChild("admin_comment"));

  private final Sites sites;
  private final OfferLoader offers;

  @Inject
  public PlacementResource(Sites sites, OfferLoader offers) {
    this.sites = sites;
    this.offers = offers;
  }

  @GET
  @Transactional
  public Element listPlacements(@QueryParam("aff_id") Long affId,
                               @QueryParam("offer_id") Long offerId,
                               @QueryParam("offset") int offset,
                               @QueryParam("limit") @DefaultValue("20") int limit) {
    return toPlacementXml(sites.listPlacements(affId, offerId, offset, limit));
  }


  @POST
  @Transactional
  public Response placeOffer(@FormParam("site_id") Long siteId,
                             @FormParam("offer_id") Long offerId,
                             @FormParam("back_url") String backUrl,
                             @FormParam("postback_url") String postbacUrl) {
    if (offerId == null || siteId == null)
      throw new WebApplicationException(400);
    Offer offer = offers.activeOfferById(offerId);
    Site site = sites.approvedSite(siteId);
    if (offer == null || site == null) throw new WebApplicationException(404);
    if (sites.findPlacement(site, offer) != null)
      throw new WebApplicationException(409);
    Placement placement = new Placement()
        .setOffer(offer)
        .setSite(site)
        .setBackUrl(backUrl)
        .setPostbackUrl(postbacUrl);
    sites.addPlacement(placement);
    return Response.ok().build();
  }



  @PUT
  @Path("{id}/moderate")
  @Transactional
  public Response moderateOfferSite(@PathParam("id") Long offerSiteId,
                                    @FormParam("admin_state")
                                    @DefaultValue("MODERATION")
                                    AdminState state,
                                    @FormParam("admin_comment")
                                    String adminComment) {
    Placement placement = sites.getOfferSite(offerSiteId);
    if (placement == null) throw new WebApplicationException(404);
    sites.moderate(placement, state, adminComment);
    return Response.ok().build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response updatePlacement(@PathParam("id") Long id,
                                  @FormParam("back_url") String backUrl,
                                  @FormParam("postback_url") String postbackUrl) {
    Placement placement = sites.getOfferSite(id);
    if (placement == null) throw new WebApplicationException(404);
    placement.setBackUrl(coalesce(backUrl, placement.backUrl()))
        .setPostbackUrl(coalesce(postbackUrl, placement.postBackUrl()))
        .touch();
    sites.put(placement);
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  @Transactional
  public Element getPlacement(@PathParam("id") Long id) {
    Map<String, Object> result = sites.getPlacementAsMap(id);
    if (result.isEmpty()) throw new WebApplicationException(404);
    return PLACEMENT_MAPPER.execute(result);
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response deletePlacement(@PathParam("id") Long id) {
    Placement placement = sites.getOfferSite(id);
    if (placement == null) throw new WebApplicationException(404);
    sites.removeOfferSite(placement);
    return Response.ok().build();
  }



  private Element toPlacementXml(Pair<QueryResult, Long> result) {
    return new QueryResultToXml()
        .setElementName("placements")
        .setAttribute("count", result.snd.toString())
        .setMapper(PLACEMENT_MAPPER)
        .execute(result.fst);
  }

  private <T> T coalesce(T first, T second) {
    if (first == null) return second;
    return first;
  }

}
