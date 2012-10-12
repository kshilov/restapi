package com.heymoose.infrastructure.service.yml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Products;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class ProductYmlImporter {

  private static final Splitter DOT = Splitter.on('.');
  private static final Logger log =
      LoggerFactory.getLogger(ProductYmlImporter.class);

  private final Repo repo;
  private final Products products;

  @Inject
  public ProductYmlImporter(Repo repo, Products products) {
    this.repo = repo;
    this.products = products;
  }

  @Transactional
  public void doImport(Document document, Long parentOfferId) {
    log.info("Entring YML import for offer: {}.", parentOfferId);
    com.heymoose.domain.offer.Offer parentOffer =
        repo.get(com.heymoose.domain.offer.Offer.class, parentOfferId);
    Preconditions.checkNotNull(parentOffer, "Offer does not exist.");
    HashMap<String, ShopCategory> categoryMap = Maps.newHashMap();
    // importing categories
    for (Element category : listCategories(document)) {
      String originalId = category.getAttributeValue("id");
      ShopCategory shopCategory =
          products.categoryByOriginalId(parentOfferId, originalId);
      if (shopCategory == null) {
        shopCategory = new ShopCategory();
        repo.put(shopCategory
            .setOriginalId(originalId)
            .setName(category.getText())
            .setOfferId(parentOfferId));
        log.info("Product category saved: {}", shopCategory);
      }
      categoryMap.put(originalId, shopCategory);
    }
    // setting correct parent for all categories
    for (Element category : listCategories(document)) {
      ShopCategory shopCategory =
          categoryMap.get(category.getAttributeValue("id"));
      String parentOriginalId = category.getAttributeValue("parentId");
      if (parentOriginalId == null) continue;
      shopCategory.setParent(categoryMap.get(parentOriginalId));
      repo.put(shopCategory);
      log.info("Parent updated for category: {}", shopCategory);
    }
    // importing products
    for (Element offer : listOffers(document)) {
      String originalId = offer.getAttributeValue("id");
      Product product = products.productByOriginalId(parentOfferId, originalId);
      if (product == null) product = new Product();
      String categoryOriginalId = offer.getChildText("categoryId");
      Element exclusiveElement = (Element) XPathFactory.instance()
          .compile("param[@name='hm_exclusive']")
          .evaluateFirst(offer);
      boolean exclusive = exclusiveElement != null &&
          Boolean.valueOf(exclusiveElement.getText());
      product.setCategory(categoryMap.get(categoryOriginalId))
          .setName(getTitle(offer))
          .setOffer(parentOffer)
          .setOriginalId(offer.getAttributeValue("id"))
          .setPrice(new BigDecimal(offer.getChildText("price")))
          .setExclusive(exclusive)
          .setUrl(offer.getChildText("url"));
      Element revenueElement = (Element) XPathFactory.instance()
          .compile("param[@name='hm_value']")
          .evaluateFirst(offer);
      if (revenueElement != null) {
        String unitUpper = revenueElement.getAttributeValue("unit").toUpperCase();
        BigDecimal revenue = new BigDecimal(revenueElement.getText());
        products.setRevenue(product, CpaPolicy.valueOf(unitUpper), revenue);
      }
      repo.put(product);
      log.info("Product saved: {}", product);
      // importing attributes
      for (Element offerChild : offer.getChildren()) {
        products.clearAttributes(product);
        ProductAttribute productAttribute = new ProductAttribute()
            .setProductId(product.id())
            .setKey(offerChild.getName())
            .setValue(offerChild.getText());
        for (Attribute attr : offerChild.getAttributes()) {
          productAttribute.addExtraInfo(attr.getName(), attr.getValue());
        }
        repo.put(productAttribute);
        log.info("Product attribute saved: {}.", productAttribute);
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
