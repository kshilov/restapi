package com.heymoose.resource;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.persistence.UserRepositoryHiber;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.service.Sites;
import com.heymoose.infrastructure.service.yml.YmlWriter;
import com.heymoose.infrastructure.util.Cacheable;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static com.heymoose.domain.base.IdEntity.toMap;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Singleton
@Path("products")
public class ProductResource {

  private final Products products;
  private final OfferGrantRepository grants;
  private final UserRepositoryHiber users;
  private final String trackerHost;
  private final Sites sites;
  private final Repo repo;

  @Inject
  public ProductResource(Repo repo, Products products,
                         OfferGrantRepository grants,
                         UserRepositoryHiber users,
                         Sites sites,
                         @Named("tracker.host") String trackerHost) {
    this.repo = repo;
    this.products = products;
    this.sites = sites;
    this.grants = grants;
    this.users = users;
    this.trackerHost = trackerHost;
  }


  @GET
  @Path("feed")
  @Produces("application/xml")
  @Cacheable
  @Transactional
  public Response feed(@QueryParam("key") String key,
                       @QueryParam("site") Long siteId,
                       @QueryParam("s") List<Long> offerList,
                       @QueryParam("c") List<Long> categoryList,
                       @QueryParam("q") String queryString,
                       @QueryParam("offset") @DefaultValue("0") int offset,
                       @QueryParam("limit") @DefaultValue("1000") int limit)
      throws Exception {
    if (Strings.isNullOrEmpty(key)) {
      return status(400, "Request should contain 'key' parameter.");
    }

    if (key.length() != User.SECRET_KEY_LENGTH) {
      return status(400, "'key' parameter length is incorrect.");
    }

    if (limit > 1000 || limit < 0) {
      return status(400, "'limit' should be between 0 and 1000.");
    }

    final User user = users.bySecretKey(key);
    if (user == null) {
      return status(401, "No user found for given 'key'.");
    }

    Site site = null;
    if (siteId != null) {
      site = sites.approvedSite(siteId);
      if (site == null || site.affiliate() != user)
        return status(400, "No active site found with id: " + siteId);
    }

    final Iterable<Product> productList =
        products.list(user, site, offerList, categoryList, queryString,
            offset, limit);
    final Iterable<ShopCategory> shopCategoryList =
        products.categoryList(user, site, offerList, categoryList, queryString);
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    new YmlWriter(byteStream)
        .setSite(site)
        .setCategoryList(shopCategoryList)
        .setProductList(productList)
        .setTrackerHost(this.trackerHost)
        .setUser(user)
        .write();

      return Response.ok().entity(new String(byteStream.toByteArray())).build();
  }

  @GET
  @Path("feed/size")
  @Transactional
  public String feedSize(@QueryParam("key") String key,
                         @QueryParam("site") Long siteId,
                         @QueryParam("s") List<Long> offerList,
                         @QueryParam("c") List<Long> categoryList,
                         @QueryParam("q") String queryString) {
    checkNotNull(key);

    final User user = users.bySecretKey(key);
    if (user == null) throw new WebApplicationException(401);
    Site site = null;
    if (siteId != null) {
      site = sites.approvedSite(siteId);
      if (site == null || site.affiliate() != user)
        throw new WebApplicationException(400);
    }

    return products
        .count(user, site, offerList, categoryList, queryString)
        .toString();
  }


  @GET
  @Path("categories")
  @Produces("application/xml")
  @Transactional
  @Cacheable(period = "PT1H") // cache for 1 hour
  public String categoryList(@QueryParam("aff_id") Long affId) {
    if (affId == null) throw new WebApplicationException(400);
    Iterable<Offer> grantedProductOffers =
        grants.grantedProductOffers(affId);

    Element result = new Element("result");
    for (Offer offer : grantedProductOffers) {
      Element offerElement = new Element("offer");

      List<ShopCategory> categoryList = products.categoryList(offer.id());
      if (categoryList.isEmpty()) continue;
      Map<Long, ShopCategory> categoryMap =
          toMap(products.categoryList(offer.id()));

      offerElement.addContent(new Element("name").setText(offer.name()));
      offerElement.addContent(new Element("yml-url").setText(offer.ymlUrl()));
      offerElement.addContent(toXmlCategories(categoryMap));
      offerElement.setAttribute("id", offer.id().toString());

      result.addContent(offerElement);
    }
    return wrapRoot(result);
  }

  private String wrapRoot(Element root) {
    return new XMLOutputter().outputString(new Document(root));
  }

  private Element toXmlCategories(Map<Long, ShopCategory> categoryMap) {
    return toXmlCategories(new Element("categories"), categoryMap);
  }

  private Element toXmlCategories(Element categories,
                                  Map<Long, ShopCategory> categoryMap) {
    for (ShopCategory category : categoryMap.values()) {
      Element categoryElement = new Element("category");
      categoryElement.setAttribute("id", category.id().toString());
      if (category.parentId() != null) {
        categoryElement.setAttribute(
            "parentId", category.parentId().toString());
      }
      categoryElement.setText(category.name());
      categories.addContent(categoryElement);
    }
    return categories;
  }

  private Response status(int status, String body) {
    return Response.status(status)
        .entity(body)
        .build();
  }
}
