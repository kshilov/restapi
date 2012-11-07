package com.heymoose.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.persistence.UserRepositoryHiber;
import com.heymoose.infrastructure.service.Products;
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
  private final Repo repo;

  @Inject
  public ProductResource(Repo repo, Products products,
                         OfferGrantRepository grants,
                         UserRepositoryHiber users,
                         @Named("tracker.host") String trackerHost) {
    this.repo = repo;
    this.products = products;
    this.grants = grants;
    this.users = users;
    this.trackerHost = trackerHost;
  }


  @GET
  @Path("feed")
  @Produces("application/xml")
  @Cacheable
  @Transactional
  public String feed(@QueryParam("key") String key,
                     @QueryParam("s") List<Long> offerList,
                     @QueryParam("c") List<Long> categoryList,
                     @QueryParam("q") String queryString,
                     @QueryParam("offset") @DefaultValue("0") int offset,
                     @QueryParam("limit") @DefaultValue("1000") int limit)
      throws Exception {
    checkNotNull(key);
    if (limit > 1000) throw new WebApplicationException(400);

    final User user = users.bySecretKey(key);
    if (user == null) throw new WebApplicationException(401);

    final Iterable<Product> productList =
        products.list(user, offerList, categoryList, queryString,
            offset, limit);
    final Iterable<ShopCategory> shopCategoryList =
        products.categoryList(user, offerList, categoryList, queryString);
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    new YmlWriter(byteStream)
        .setCategoryList(shopCategoryList)
        .setProductList(productList)
        .setTrackerHost(this.trackerHost)
        .setUser(user)
        .write();

      return new String(byteStream.toByteArray());
  }

  @GET
  @Path("feed/size")
  @Transactional
  public String feedSize(@QueryParam("key") String key,
                         @QueryParam("s") List<Long> offerList,
                         @QueryParam("c") List<Long> categoryList,
                         @QueryParam("q") String queryString) {
    checkNotNull(key);

    final User user = users.bySecretKey(key);
    if (user == null) throw new WebApplicationException(401);

    return products.count(user, offerList, categoryList, queryString).toString();
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

}
