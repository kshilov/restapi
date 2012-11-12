package com.heymoose.resource;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Category;
import com.heymoose.domain.site.BlackListEntry;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.user.Lang;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.BlackList;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.TypedMap;
import com.heymoose.infrastructure.util.db.QueryResult;
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
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;
import static com.heymoose.resource.Exceptions.notFound;

@Singleton
@Path("sites")
public class SiteResource {

  private static final XMLOutputter XML_OUTPUTTER = new XMLOutputter();

  private final Repo repo;
  private final Sites sites;
  private final BlackList blackList;

  @Inject
  public SiteResource(Repo repo, Sites sites, BlackList blackList) {
    this.repo = repo;
    this.sites = sites;
    this.blackList = blackList;
  }

  @POST
  @Transactional
  public void register(@FormParam("userId") Long userId,
                       @FormParam("name") String name,
                       @FormParam("domain") String domain,
                       @FormParam("lang") Lang lang,
                       @FormParam("comment") String comment,
                       @FormParam("category") List<Long> categories,
                       @FormParam("region") List<String> regions) {

    checkNotNull(userId, name, domain, lang, comment);
    User user = repo.get(User.class, userId);
    if (user == null)
      throw notFound();
    Map<Long, Category> categoryMap = repo.get(Category.class, newHashSet(categories));
    Site site = new Site(name, domain, lang, comment, user, newHashSet(categoryMap.values()), newHashSet(regions));
    repo.put(site);
  }

  @GET
  @Path("stats")
  @Produces("application/xml")
  @Transactional
  public String stats(@QueryParam("first_period_from") Long firstPeriodFrom,
                      @QueryParam("first_period_to") Long firstPeriodTo,
                      @QueryParam("second_period_from") Long secondPeriodFrom,
                      @QueryParam("second_period_to") Long secondPeriodTo,
                      @QueryParam("offset") int offset,
                      @QueryParam("limit") @DefaultValue("20") int limit) {
    DateTime firstFromDate = new DateTime(firstPeriodFrom);
    DateTime firstToDate = new DateTime(firstPeriodTo);
    DateTime secondFromDate = new DateTime(secondPeriodFrom);
    DateTime secondToDate = new DateTime(secondPeriodTo);
    QueryResult queryResult = sites.stats(
        firstFromDate, firstToDate,
        secondFromDate, secondToDate,
        offset, limit);

    Element root = new Element("stats");
    for (Map<String, Object> entry : queryResult) {
      TypedMap map = TypedMap.wrap(entry);
      Element stat = new Element("stat");
      Element aff = new Element("affiliate")
          .addContent(element("id", map.getString("affiliate_id")))
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
}
