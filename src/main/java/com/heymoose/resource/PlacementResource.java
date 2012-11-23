package com.heymoose.resource;

import com.google.inject.Inject;
import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.OfferSite;
import com.heymoose.domain.site.Site;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.TypedMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.resource.xml.JDomUtil;
import org.jdom2.Element;

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

import static com.heymoose.resource.xml.JDomUtil.element;

@Path("placements")
public class PlacementResource {

  private final Sites sites;
  private final OfferLoader offers;

  @Inject
  public PlacementResource(Sites sites, OfferLoader offers) {
    this.sites = sites;
    this.offers = offers;
  }

  @GET
  @Transactional
  public String listPlacements(@QueryParam("aff_id") Long affId,
                               @QueryParam("offer_id") Long offerId,
                               @QueryParam("offset") int offset,
                               @QueryParam("limit") @DefaultValue("20") int limit) {
    return toPlacementXml(sites.listOfferSites(affId, offerId, offset, limit));
  }


  @POST
  @Transactional
  public Response placeOffer(@FormParam("site_id") Long siteId,
                             @FormParam("offer_id") Long offerId,
                             @FormParam("back_url") String backUrl,
                             @FormParam("postback_url") String postbacUrl) {
    if (offerId == null) throw new WebApplicationException(400);
    Offer offer = offers.activeOfferById(offerId);
    Site site = sites.approvedSite(siteId);
    if (offer == null || site == null) throw new WebApplicationException(404);
    OfferSite offerSite = new OfferSite()
        .setOffer(offer)
        .setSite(site)
        .setBackUrl(backUrl)
        .setPostbackUrl(postbacUrl);
    sites.addOfferSite(offerSite);
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
    OfferSite offerSite = sites.getOfferSite(offerSiteId);
    if (offerSite == null) throw new WebApplicationException(404);
    sites.moderate(offerSite, state, adminComment);
    return Response.ok().build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response updatePlacement(@PathParam("id") Long id,
                                  @FormParam("back_url") String backUrl,
                                  @FormParam("postback_url") String postbackUrl) {
    OfferSite offerSite = sites.getOfferSite(id);
    if (offerSite == null) throw new WebApplicationException(404);
    offerSite.setBackUrl(coalesce(backUrl, offerSite.backUrl()))
        .setPostbackUrl(coalesce(postbackUrl, offerSite.postBackUrl()))
        .touch();
    sites.put(offerSite);
    return Response.ok().build();
  }



  private String toPlacementXml(Pair<QueryResult, Long> result) {
    Element root = new Element("placements")
        .setAttribute("count", result.snd.toString());
    for (Map<String, Object> map : result.fst) {
      TypedMap entry = TypedMap.wrap(map);
      Element entryXml = new Element("placement")
          .setAttribute("id", entry.getString("id"))
          .addContent(element("admin-state", entry.getString("admin_state")))
          .addContent(element("admin-comment", entry.getString("admin_comment")))
          .addContent(element("creation-time", entry.getDateTime("creation_time")))
          .addContent(element("last-change-time", entry.getDateTime("last_change_time")))
          .addContent(element("back-url", entry.getString("back_url")))
          .addContent(element("postback-url", entry.getString("postback_url")));
      Element offerXml = new Element("offer")
          .setAttribute("id", entry.getString("offer_id"))
          .addContent(element("name", entry.getString("offer_name")));
      Element affiliateXml = new Element("affiliate")
          .setAttribute("id", entry.getString("affiliate_id"))
          .addContent(element("email", entry.getString("affiliate_email")))
          .addContent(element("blocked", entry.getString("affiliate_blocked")));
      Element siteXml = new Element("site")
          .setAttribute("id", entry.getString("site_id"))
          .addContent(element("type", entry.getString("site_type")))
          .addContent(element("name", entry.getString("site_name")));
      entryXml.addContent(offerXml)
          .addContent(affiliateXml)
          .addContent(siteXml);
      root.addContent(entryXml);
    }
    return JDomUtil.toXmlString(root);
  }

  private <T> T coalesce(T first, T second) {
    if (first == null) return second;
    return first;
  }

}
