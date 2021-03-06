package com.heymoose.resource;

import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.base.AdminState;
import com.heymoose.domain.site.BlackListEntry;
import com.heymoose.domain.site.Site;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.BlackList;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.MapToXml;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResultToXml;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.resource.xml.JDomUtil;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;
import org.jdom2.Element;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.heymoose.resource.xml.JDomUtil.element;

@Singleton
@Path("sites")
public class SiteResource {
  private final Sites sites;
  private final BlackList blackList;

  @Inject
  public SiteResource(Sites sites,
                      BlackList blackList) {
    this.sites = sites;
    this.blackList = blackList;
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

  @PUT
  @Path("{id}/moderate")
  @Transactional
  public Response moderateSite(@PathParam("id") Long siteId,
                               @FormParam("admin_state")
                               @DefaultValue("MODERATION")
                               AdminState state,
                               @FormParam("admin_comment") String adminComment) {
    Site site = sites.get(siteId);
    if (site == null) throw new WebApplicationException(404);
    sites.moderate(site, state, adminComment);
    return Response.ok().build();
  }


  @GET
  @Transactional
  public Element listSites(@QueryParam("aff_id") Long affId,
                           @QueryParam("admin_state") AdminState state,
                           @QueryParam("offset") int offset,
                           @QueryParam("limit") @DefaultValue("20") int limit,
                           @QueryParam("ordering") @DefaultValue("LAST_CHANGE_TIME")
                           Sites.Ordering ordering,
                           @QueryParam("direction") @DefaultValue("ASC")
                           OrderingDirection direction) {
    DataFilter<Sites.Ordering> common = DataFilter.newInstance();
    common.setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return toSiteXml(sites.list(affId, state, common));
  }

  @GET
  @Path("{id}")
  @Transactional
  public Element getSite(@PathParam("id") Long siteId) {
    Site site = sites.get(siteId);
    if (site == null) throw new WebApplicationException(404);
    return toSiteXml(site);
  }

  @GET
  @Path("stats")
  @Transactional
  public Element stats(@QueryParam("first_period_from") Long firstPeriodFrom,
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

    return new QueryResultToXml()
        .setElementName("stats")
        .setAttribute("count", count.toString())
        .setMapper(new MapToXml()
            .setElementName("stat")
            .addSubMapper(new MapToXml()
                .setElementName("affiliate")
                .addAttribute("id")
                .addChild("email"))
            .addChild("referer")
            .addChild("first_period_show_count")
            .addChild("second_period_show_count")
            .addChild("show_count_diff")
            .addChild("first_period_click_count")
            .addChild("second_period_click_count")
            .addChild("click_count_diff"))
        .execute(result);
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
        .entity(JDomUtil.XML_OUTPUTTER.outputString(blackListEntryXml(entry)))
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
    return JDomUtil.XML_OUTPUTTER.outputString(root);
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

  private Element toSiteXml(Pair<List<Site>, Long> result) {
    Element root = new Element("sites")
        .setAttribute("count", result.snd.toString());
    for (Site site : result.fst) {
      root.addContent(toSiteElement(site));
    }
    return root;
  }

  private Element toSiteXml(Site site) {
    return toSiteElement(site);
  }

  private Element toSiteElement(Site site) {
    Element siteElement = new Element("site")
        .setAttribute("id", site.id().toString());
    Element aff = new Element("affiliate")
        .setAttribute("id", site.affiliate().id().toString())
        .addContent(element("email", site.affiliate().email()))
        .addContent(element("blocked", site.affiliate().blocked()));
    siteElement.addContent(aff)
        .addContent(element("name", site.name()))
        .addContent(element("description", site.description()))
        .addContent(element("type", site.type()))
        .addContent(element("admin-state", site.adminState()))
        .addContent(element("admin-comment", site.adminComment()))
        .addContent(element("creation-time", site.creationTime()))
        .addContent(element("last-change-time", site.lastChangeTime()));
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
    checkNotNull(affId, name, type);
    return new Site(Site.Type.valueOf(type))
        .setName(name)
        .setAffId(affId)
        .setDescription(description)
        .addAttributesFromMap(siteAttributes.build());
  }

  private void checkNotNull(Object... list) {
    for (Object o : list) {
      if (o == null) throw new WebApplicationException(400);
    }
  }

}
