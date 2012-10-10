package com.heymoose.resource;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Products;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.List;

@Path("products")
public class ProductResource {

  private final Products products;

  @Inject
  public ProductResource(Products products)  {
    this.products = products;
  }


  @GET
  @Path("feed")
  @Produces("application/xml")
  @Transactional
  public String feed(@QueryParam("offer_id") Long offerId,
                           @QueryParam("category") List<Long> categoryList,
                           @QueryParam("q") String queryString) {
    return toYml(products.list(offerId, categoryList, queryString));
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
      offer.getChild("url").setText("http://heymoose.com");
      offer.getChild("categoryId").setText(product.category().id().toString());
      offer.setAttribute("id", product.id().toString());
      offers.addContent(offer);

      ShopCategory category = product.category();
      while (category.parent() != null) {
        categoryMap.put(category.id(), category);
        category = category.parent();
      }
      categoryMap.put(category.id(), category);
    }
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
    Element catalog = new Element("yml_catalog");
    catalog.setAttribute("date", DateTime.now().toString());
    catalog.addContent(shop);
    return new XMLOutputter().outputString(new Document(catalog));
  }

}
