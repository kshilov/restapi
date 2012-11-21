package com.heymoose.resource;

import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.site.BlackListEntry;
import com.heymoose.domain.site.OfferSite;
import com.heymoose.domain.site.Site;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.BlackList;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.TypedMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Singleton
@Path("sites")
public class SiteResource {

  private static final XMLOutputter XML_OUTPUTTER = new XMLOutputter();

  private final Sites sites;
  private final BlackList blackList;
  private final OfferLoader offers;

  @Inject
  public SiteResource(Sites sites,
                      BlackList blackList,
                      OfferLoader offers) {
    this.sites = sites;
    this.blackList = blackList;
    this.offers = offers;
  }

  @POST
  @Transactional
  public Response register(@Context HttpContext context) {
    Site site = parseSiteForm(context);
    sites.add(site);
    try {
      return Response
          .created(new URI(site.id().toString()))
          .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Response updateSite(@PathParam("id") Long siteId,
                             @Context HttpContext context) {
    if (siteId == null) throw new WebApplicationException(400);
    Site site = sites.get(siteId);
    if (site == null) throw new WebApplicationException(404);
    Site updatedSite = parseSiteForm(context);
    sites.merge(updatedSite, site);
    return Response.ok().build();
  }

  @POST
  @Path("placements")
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
  @Path("placements/{id}")
  @Transactional
  public Response updatePlacement(@PathParam("id") Long id,
                                  @FormParam("back_url") String backUrl,
                                  @FormParam("postback_url") String postbackUrl,
                                  @FormParam("approved") Boolean approved) {
    OfferSite offerSite = sites.getOfferSite(id);
    if (offerSite == null) throw new WebApplicationException(404);
    offerSite.setBackUrl(coalesce(backUrl, offerSite.backUrl()))
        .setPostbackUrl(coalesce(postbackUrl, offerSite.postBackUrl()))
        .setApprovedByAdmin(coalesce(approved, offerSite.approvedByAdmin()));
    sites.put(offerSite);
    return Response.ok().build();
  }

  @PUT
  @Path("{id}/approve")
  @Transactional
  public Response approveSite(@PathParam("id") Long siteId) {
    Site site = sites.get(siteId);
    if (site == null) throw new WebApplicationException(404);
    sites.put(site.adminApprove());
    return Response.ok().build();
  }


  @PUT
  @Path("placement/{id}/approve")
  @Transactional
  public Response approveOfferSite(@PathParam("id") Long offerSiteId) {
    OfferSite offerSite = sites.getOfferSite(offerSiteId);
    if (offerSite == null) throw new WebApplicationException(404);
    sites.put(offerSite.adminApprove());
    return Response.ok().build();
  }

  @GET
  @Produces("application/xml")
  @Transactional
  public String listSites(@QueryParam("aff_id") Long affId,
                          @QueryParam("offset") int offset,
                          @QueryParam("limit") @DefaultValue("20") int limit,
                          @QueryParam("ordering") @DefaultValue("AFFILIATE_EMAIL")
                          Sites.Ordering ordering,
                          @QueryParam("direction") @DefaultValue("ASC")
                          OrderingDirection direction) {
    DataFilter<Sites.Ordering> common = DataFilter.newInstance();
    common.setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toSiteXml(sites.list(affId, common));
  }

  @GET
  @Path("{id}")
  @Produces("application/xml")
  @Transactional
  public String getSite(@PathParam("id") Long siteId) {
    Site site = sites.get(siteId);
    if (site == null) throw new WebApplicationException(404);
    return toSiteXml(site);
  }

  @GET
  @Path("placements")
  @Transactional
  public String listPlacements(@QueryParam("aff_id") Long affId,
                               @QueryParam("offer_id") Long offerId,
                               @QueryParam("offset") int offset,
                               @QueryParam("limit") @DefaultValue("20") int limit) {
    return toPlacementXml(sites.listOfferSites(affId, offerId, offset, limit));
  }

  private String toPlacementXml(Pair<QueryResult, Long> result) {
    Element root = new Element("placements")
        .setAttribute("count", result.snd.toString());
    for (Map<String, Object> map : result.fst) {
      TypedMap entry = TypedMap.wrap(map);
      Element entryXml = new Element("placement")
          .setAttribute("id", entry.getString("id"))
          .addContent(element("approved", entry.getString("approved")))
          .addContent(element("back-url", entry.getString("back_url")))
          .addContent(element("postback-url", entry.getString("postback_url")));
      Element offerXml = new Element("offer")
          .setAttribute("id", entry.getString("offer_id"))
          .addContent(element("title", entry.getString("offer_title")));
      Element affiliateXml = new Element("affiliate")
          .setAttribute("id", entry.getString("affiliate_id"))
          .addContent(element("email", entry.getString("affiliate_email")));
      Element siteXml = new Element("site")
          .setAttribute("id", entry.getString("site_id"))
          .addContent(element("type", entry.getString("site_type")))
          .addContent(element("name", entry.getString("site_name")));
      entryXml.addContent(offerXml)
          .addContent(affiliateXml)
          .addContent(siteXml);
      root.addContent(entryXml);
    }
    return XML_OUTPUTTER.outputString(root);
  }


  @GET
  @Path("stats")
  @Produces("application/xml")
  @Transactional
  public String stats(@QueryParam("first_period_from") Long firstPeriodFrom,
                      @QueryParam("first_period_to") Long firstPeriodTo,
                      @QueryParam("second_period_from") Long secondPeriodFrom,
                      @QueryParam("second_period_to") Long secondPeriodTo,
                      @QueryParam("removed_only") @DefaultValue("false")
                      boolean removedOnly,
                      @QueryParam("ordering") @DefaultValue("CLICK_COUNT_DIFF")
                      Sites.StatOrdering ordering,
                      @QueryParam("direction") @DefaultValue("ASC")
                      OrderingDirection direction,
                      @QueryParam("offset") int offset,
                      @QueryParam("limit") @DefaultValue("20") int limit) {
    DateTime firstFromDate = new DateTime(firstPeriodFrom);
    DateTime firstToDate = new DateTime(firstPeriodTo);
    DateTime secondFromDate = new DateTime(secondPeriodFrom);
    DateTime secondToDate = new DateTime(secondPeriodTo);
    QueryResult result = sites.stats(
        firstFromDate, firstToDate,
        secondFromDate, secondToDate,
        removedOnly,
        ordering, direction,
        offset, limit);
    Long count = sites.statsCount(
        firstFromDate, firstToDate,
        secondFromDate, secondToDate,
        removedOnly);

    Element root = new Element("stats")
        .setAttribute("count", count.toString());
    for (Map<String, Object> entry : result) {
      TypedMap map = TypedMap.wrap(entry);
      Element stat = new Element("stat");
      Element aff = new Element("affiliate")
          .setAttribute("id", map.getString("affiliate_id"))
          .addContent(element("email", map.getString("affiliate_email")));
      stat.addContent(aff)
          .addContent(element("referer", map.getString("referer")))
          .addContent(element(
              "first-period-show-count",
              map.getString("first_period_show_count")))
          .addContent(element(
              "second-period-show-count",
              map.getString("second_period_show_count")))
          .addContent(element(
              "show-count-diff",
              map.getString("show_count_diff")))
          .addContent(element(
              "first-period-click-count",
              map.getString("first_period_click_count")))
          .addContent(element(
              "second-period-click-count",
              map.getString("second_period_click_count")))
          .addContent(element(
              "click-count-diff",
              map.getString("click_count_diff")));
      root.addContent(stat);
    }
    return XML_OUTPUTTER.outputString(root);
  }

  @PUT
  @Path("blacklist/{id}")
  @Transactional
  public Response updateBlackList(@PathParam("id") Long id,
                                  @FormParam("host") String host,
                                  @FormParam("path_mask") String pathMask,
                                  @FormParam("sub_domain_mask") String subDomainMask,
                                  @FormParam("comment") String comment) {
    if (host == null) throw new WebApplicationException(400);
    BlackListEntry entry = new BlackListEntry()
        .setHost(host)
        .setPathMask(pathMask)
        .setSubDomainMask(subDomainMask)
        .setComment(comment)
        .setId(id);
    blackList.put(entry).id();
    return Response.ok().build();
  }

  @POST
  @Path("blacklist")
  @Transactional
  public Response createBlackListEntry(@FormParam("host") String host,
                                       @FormParam("path_mask") String pathMask,
                                       @FormParam("sub_domain_mask")
                                       String subDomainMask,
                                       @FormParam("comment") String comment) {
    if (host == null) throw new WebApplicationException(400);
    BlackListEntry entry = new BlackListEntry()
        .setHost(host)
        .setPathMask(pathMask)
        .setSubDomainMask(subDomainMask)
        .setComment(comment);
    Long newId = blackList.put(entry).id();
    try {
      return Response.created(new URI("/" + newId)).build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @DELETE
  @Path("blacklist/{id}")
  @Transactional
  public Response removeBlackListEntry(@PathParam("id") Long id) {
    if (id == null) throw new WebApplicationException(400);
    if (blackList.remove(id)) return Response.ok().build();
    return Response.status(404).build();
  }


  @GET
  @Path("blacklist/{id}")
  @Transactional
  public Response blackListEntryById(@PathParam("id") Long id) {
    BlackListEntry entry = blackList.getById(id);
    if (entry == null) return Response.status(404).build();
    return Response.ok()
        .entity(XML_OUTPUTTER.outputString(blackListEntryXml(entry)))
        .build();
  }

  @GET
  @Path("blacklist")
  @Transactional
  public String blackList(@QueryParam("host") String host,
                          @QueryParam("offset") int offset,
                          @QueryParam("limit") @DefaultValue("20") int limit) {
    Pair<? extends Iterable<BlackListEntry>, Long> result;
    if (host != null) {
      result = blackList.getByHost(host, offset, limit);
    } else {
      result = blackList.all(offset, limit);
    }
    return toXml(result);
  }

  private String toXml(Pair<? extends Iterable<BlackListEntry>, Long> blackList) {
    Long count = blackList.snd;
    Element root = new Element("blacklist");
    root.setAttribute("count", count.toString());
    for (BlackListEntry entry : blackList.fst) {
      root.addContent(blackListEntryXml(entry));
    }
    return XML_OUTPUTTER.outputString(root);
  }

  private Element blackListEntryXml(BlackListEntry entry) {
    Element xmlEntry = new Element("entry")
        .setAttribute("id", entry.id().toString());
    return xmlEntry
        .addContent(new Element("host").setText(entry.host()))
        .addContent(new Element("path-mask").setText(entry.pathMask()))
        .addContent(new Element("sub-domain-mask").setText(entry.subDomainMask()))
        .addContent(new Element("comment").setText(entry.comment()));
  }

  private Element element(String name, String text) {
    return new Element(name).setText(text);
  }

  private String toSiteXml(Pair<List<Site>, Long> result) {
    Element root = new Element("sites")
        .setAttribute("count", result.snd.toString());
    for (Site site : result.fst) {
      root.addContent(toSiteElement(site));
    }
    return XML_OUTPUTTER.outputString(root);
  }

  private String toSiteXml(Site site) {
    return XML_OUTPUTTER.outputString(toSiteElement(site));
  }

  private Element toSiteElement(Site site) {
    Element siteElement = new Element("site")
        .setAttribute("id", site.id().toString());
    Element aff = new Element("affiliate")
        .setAttribute("id", site.affiliate().id().toString())
        .addContent(element("email", site.affiliate().email()));
    siteElement.addContent(aff);
    siteElement.addContent(element("name", site.name()));
    siteElement.addContent(element("description", site.description()));
    siteElement.addContent(element("type", site.type().toString()));
    siteElement.addContent(element("approved",
        String.valueOf(site.approvedByAdmin())));
    for (Map.Entry<String, String> entry : site.attributeMap().entrySet()) {
      siteElement.addContent(element(entry.getKey(), entry.getValue()));
    }
    return siteElement;
  }

  private Site parseSiteForm(HttpContext context) {
    Form form = context.getRequest().getEntity(Form.class);
    String name = null;
    String type = null;
    Long affId = null;
    String description = null;
    ImmutableMap.Builder<String, String> siteAttributes =
        ImmutableMap.builder();
    for (Map.Entry<String, List<String>> entry : form.entrySet()) {
      if (entry.getKey().equals("aff_id")) {
        affId = Long.valueOf(entry.getValue().get(0));
        continue;
      }
      if (entry.getKey().equals("type")) {
        type = entry.getValue().get(0);
        continue;
      }
      if (entry.getKey().equals("name")) {
        name = entry.getValue().get(0);
        continue;
      }
      if (entry.getKey().equals("description")) {
        description = entry.getValue().get(0);
        continue;
      }
      siteAttributes.put(entry.getKey(), entry.getValue().get(0));
    }
    return new Site(Site.Type.valueOf(type))
        .setName(name)
        .setAffId(affId)
        .setDescription(description)
        .addAttributesFromMap(siteAttributes.build());
  }

  private <T> T coalesce(T first, T second) {
    if (first == null) return second;
    return first;
  }
}
