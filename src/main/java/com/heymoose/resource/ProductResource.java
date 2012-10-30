package com.heymoose.resource;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.persistence.UserRepositoryHiber;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.util.Cacheable;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.joda.time.DateTime;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
  private final String urlMask;

  @Inject
  public ProductResource(Products products,
                         OfferGrantRepository grants,
                         UserRepositoryHiber users,
                         @Named("tracker.host") String trackerHost) {
    this.products = products;
    this.grants = grants;
    this.users = users;
    this.urlMask = trackerHost +
        "/api?method=click&offer_id=%s&aff_id=%s&ulp=%s";
  }


  @GET
  @Path("feed")
  @Produces("application/xml")
  @Transactional
  @Cacheable(period = "PT1H") // cache for 1 hour
  public String feed(@QueryParam("key") String key,
                     @QueryParam("s") List<Long> offerList,
                     @QueryParam("c") List<Long> categoryList,
                     @QueryParam("q") String queryString,
                     @QueryParam("p") @DefaultValue("0") int page) {
    checkNotNull(key);
    User user = users.bySecretKey(key);
    if (user == null) throw new WebApplicationException(401);
    Iterable<Offer> grantedOffers = grants.exclusiveGrantedOffers(user.id());
    Map<Long, Offer> grantedOffersMap = toMap(grantedOffers);

    Collection<Long> offersToSearch = offerList;
    if (offerList.isEmpty()) {
      // search through all granted exclusive offers
      offersToSearch = grantedOffersMap.keySet();
    } else {
      // search through all given offers, if they are granted
      Iterator<Long> offerListIterator = offerList.iterator();
      while (offerListIterator.hasNext()) {
        final Long id = offerListIterator.next();
        if (!grantedOffersMap.containsKey(id)) offerListIterator.remove();
      }
    }
    Iterable<Product> productList =
        products.list(offersToSearch, categoryList, queryString, page);
    return toYml(productList, user);
  }


  @GET
  @Path("categories")
  @Produces("application/xml")
  @Transactional
  @Cacheable(period = "PT1H") // cache for 1 hour
  public String categoryList(@QueryParam("aff_id") Long affId) {
    if (affId == null) throw new WebApplicationException(400);
    Iterable<Offer> exclusiveGrantedOffers =
        grants.exclusiveGrantedOffers(affId);

    Element result = new Element("result");
    for (Offer offer : exclusiveGrantedOffers) {
      Element offerElement = new Element("offer");

      List<ShopCategory> categoryList = products.categoryList(offer.id());
      if (categoryList.isEmpty()) continue;
      Map<Long, ShopCategory> categoryMap =
          toMap(products.categoryList(offer.id()));

      offerElement.addContent(new Element("name").setText(offer.name()));
      offerElement.addContent(toXmlCategories(categoryMap));
      offerElement.setAttribute("id", offer.id().toString());

      result.addContent(offerElement);
    }
    return wrapRoot(result);
  }

  private String toYml(Iterable<Product> productList, User user) {
    HashMap<Long, ShopCategory> categoryMap = Maps.newHashMap();
    Element shop = new Element("shop");
    shop.addContent(new Element("name").setText("HeyMoose!"));

    Element categories = new Element("categories");
    shop.addContent(categories);

    Element offers = new Element("offers");
    shop.addContent(offers);

    for (Product product : productList) {
      Element offer = new Element("offer");

      for (Map.Entry<String, String> extra : product.extraInfo().entrySet()) {
        offer.setAttribute(extra.getKey(), extra.getValue());
      }
      offer.setAttribute("id", product.id().toString());

      for (ShopCategory category : product.directCategoryList()) {
        Element categoryElement = new Element("categoryId")
            .setText(category.id().toString());
        offer.addContent(categoryElement);
      }

      for (ProductAttribute attribute : product.attributes()) {
        Element attributeElement = new Element(attribute.key())
            .setText(attribute.value());
        for (Map.Entry<String, String> extraAttr :
            attribute.extraInfo().entrySet()) {
          attributeElement.setAttribute(extraAttr.getKey(), extraAttr.getValue());
        }
        offer.addContent(attributeElement);
      }

      Element name = offer.getChild("name");
      if (name == null) {
        name = new Element("name");
        offer.addContent(name);
      }
      name.setText(product.name());
      offer.getChild("categoryId").setText(product.category().id().toString());

      String originalUrl = offer.getChildText("url");
      offer.addContent(param("hm_offer_id", product.offer().id()));
      offer.addContent(param("hm_offer_name", product.offer().name()));
      offer.addContent(param("hm_original_url", originalUrl));

      Tariff tariff = product.tariff();
      if (tariff != null) {
        Element hmRevenue = param("hm_revenue", tariff.affiliateValue())
            .setAttribute("unit", tariff.cpaPolicy().toString().toLowerCase());
        offer.addContent(hmRevenue);
      }

      String originalUrlEncoded = "";
      try {
        originalUrlEncoded = URLEncoder.encode(originalUrl, "utf-8");
      } catch (UnsupportedEncodingException e) {  }
      String newUrl = String.format(urlMask,
          product.offer().id(), user.id(), originalUrlEncoded);
      offer.getChild("url").setText(newUrl);
      offers.addContent(offer);

      for (ShopCategory category : product.categoryList()) {
        categoryMap.put(category.id(), category);
      }
    }

    toXmlCategories(categories, categoryMap);

    Element catalog = new Element("yml_catalog");
    catalog.setAttribute("date", DateTime.now().toString());
    catalog.addContent(shop);
    return wrapRoot(catalog);
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
      if (category.parent() != null) {
        categoryElement.setAttribute(
            "parentId", category.parent().id().toString());
      }
      categoryElement.setText(category.name());
      categories.addContent(categoryElement);
    }
    return categories;
  }

  private Element param(String key, Object value) {
    return new Element("param")
        .setAttribute("name", key)
        .setText(value.toString());
  }

}
