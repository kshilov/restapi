package com.heymoose.infrastructure.service.yml;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.infrastructure.persistence.Transactional;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class ProductYmlImporter {

  private static final Splitter DOT = Splitter.on('.');
  private static final Logger log =
      LoggerFactory.getLogger(ProductYmlImporter.class);

  private final Repo repo;

  @Inject
  public ProductYmlImporter(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public void doImport(Document document, Long parentOfferId) {
    log.info("Entring YML import for offer: {}.", parentOfferId);
    for (Element category : listCategories(document)) {
      ShopCategory shopCategory = repo.byHQL(
          ShopCategory.class,
          "from ShopCategory where offerId = ? and originalId = ?",
          parentOfferId, category.getAttributeValue("id"));
      if (shopCategory != null) {
        continue; // todo: not skip saved categories?
      }
      shopCategory = new ShopCategory();
      repo.put(shopCategory
          .setOriginalId(category.getAttributeValue("id"))
          .setName(category.getText())
          .setOfferId(parentOfferId)
          .setParentOriginalId(category.getAttributeValue("parentId")));
      log.info("Product category saved: {}", shopCategory);
    }
    for (Element offer : listOffers(document)) {
      String originalId = offer.getAttributeValue("id");
      Product product = repo.byHQL(
          Product.class,
          "from Product where offerId = ? and originalId = ?",
          parentOfferId, originalId);
      if (product == null) product = new Product();
      product.setCategoryOriginalId(offer.getChildText("categoryId"))
          .setName(getTitle(offer))
          .setOfferId(parentOfferId)
          .setOriginalId(offer.getAttributeValue("id"))
          .setPrice(new BigDecimal(offer.getChildText("price")))
          .setUrl(offer.getChildText("url"));
      repo.put(product);
      log.info("Product saved: {}", product);
      for (Element productAttribute : offer.getChildren()) {
        repo.session().createSQLQuery(
            "delete from product_attribute where product_id = ?")
            .setParameter(0, product.id());
        ProductAttribute attribute = new ProductAttribute()
            .setProductId(product.id())
            .setKey(productAttribute.getName())
            .setValue(productAttribute.getText());
        repo.put(attribute);
        log.info("Product attribute saved: {}.", attribute);
      }
    }
  }

  private List<Element> listCategories(Document document) {
    return document.getRootElement()
        .getChild("shop")
        .getChild("categories")
        .getChildren();
  }

  private List<Element> listOffers(Document document) {
    return document.getRootElement()
        .getChild("shop")
        .getChild("offers")
        .getChildren();
  }

  private String getTitle(Element offer) {
    String type = offer.getAttributeValue("type");
    if (type == null) return offer.getChildText("name");

    StringBuilder titleBuilder = new StringBuilder();
    String typePrefix = offer.getChildText("typePrefix");
    if (typePrefix != null) titleBuilder.append(typePrefix).append(' ');
    for (String childName : DOT.split(type)) {
      String childValue = offer.getChildText(childName);
      if (childValue != null) titleBuilder.append(childValue).append(' ');
    }
    return titleBuilder.deleteCharAt(titleBuilder.length() - 1).toString();
  }
}
