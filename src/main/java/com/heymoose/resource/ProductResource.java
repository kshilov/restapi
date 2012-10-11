package com.heymoose.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.grant.OfferGrant;
import com.heymoose.domain.grant.OfferGrantFilter;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.grant.OfferGrantState;
import com.heymoose.domain.offer.OfferRepository;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.util.Cacheable;
import com.heymoose.infrastructure.util.OrderingDirection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("products")
public class ProductResource {

  private static <T extends IdEntity> Map<Long, T> toMap(Iterable<T> list) {
    ImmutableMap.Builder<Long, T> builder = ImmutableMap.builder();
    for (T entity : list) {
      builder.put(entity.id(), entity);
    }
    return builder.build();
  }

  private final Products products;
  private final OfferGrantRepository grants;

  @Inject
  public ProductResource(Products products,
                         OfferGrantRepository grants)  {
    this.products = products;
    this.grants = grants;
  }


  @GET
  @Path("feed")
  @Produces("application/xml")
  @Transactional
  @Cacheable(period = "PT1H") // cache for 1 hour
  public String feed(@QueryParam("s") List<Long> offerList,
                     @QueryParam("c") List<Long> categoryList,
                     @QueryParam("q") String queryString) {
    return toYml(products.list(offerList, categoryList, queryString));
  }


  @GET
  @Path("categories")
  @Produces("application/xml")
  @Transactional
  @Cacheable(period = "PT1H") // cache for 1 hour
  public String categoryList(@QueryParam("aff_id") Long affId) {
    if (affId == null) throw new WebApplicationException(400);
    OfferGrantFilter filter = new OfferGrantFilter()
        .setAffiliateId(affId)
        .setBlocked(false)
        .setActive(true)
        .setExclusiveOnly(true)
        .setState(OfferGrantState.APPROVED);
    Iterable<OfferGrant> exclusiveGrants = grants.list(
        OfferRepository.Ordering.ID, OrderingDirection.ASC,
        0, Integer.MAX_VALUE, filter);

    Element result = new Element("result");
    for (OfferGrant grant : exclusiveGrants) {
      Long offerId = grant.offer().id();
      Element offer = new Element("offer");

      List<ShopCategory> categoryList = products.categoryList(offerId);
      if (categoryList.isEmpty()) continue;
      Map<Long, ShopCategory> categoryMap =
          toMap(products.categoryList(offerId));

      offer.addContent(new Element("name").setText(grant.offer().name()));
      offer.addContent(toXmlCategories(categoryMap));
      offer.setAttribute("id", offerId.toString());

      result.addContent(offer);
    }
    return wrapRoot(result);
  }

  private String toYml(Iterable<Product> productList) {
    HashMap<Long, ShopCategory> categoryMap = Maps.newHashMap();
    Element shop = new Element("shop");
    shop.addContent(new Element("name").setText("HeyMoose!"));

    Element categories = new Element("categories");
    shop.addContent(categories);

    Element offers = new Element("offers");
    shop.addContent(offers);

    for (Product product : productList) {
      Element offer = new Element("offer");
      for (ProductAttribute attribute : product.attributes()) {
        offer.addContent(new Element(attribute.key()).setText(attribute.value()));
      }
      offer.getChild("categoryId").setText(product.category().id().toString());
      offer.setAttribute("id", product.id().toString());

      offer.addContent(param("hm_offer_id", product.offer().id()));
      offer.addContent(param("hm_offer_name", product.offer().name()));
      offer.addContent(param("hm_original_url", offer.getChildText("url")));

      offer.getChild("url").setText("http://heymoose.com");
      offers.addContent(offer);

      ShopCategory category = product.category();
      while (category.parent() != null) {
        categoryMap.put(category.id(), category);
        category = category.parent();
      }
      categoryMap.put(category.id(), category);
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
