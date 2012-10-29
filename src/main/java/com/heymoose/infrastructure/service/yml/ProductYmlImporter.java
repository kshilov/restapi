package com.heymoose.infrastructure.service.yml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ProductCategoryMapping;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.service.Tariffs;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Singleton
public class ProductYmlImporter {

  private static final Splitter DOT = Splitter.on('.');
  private static final XMLOutputter OUTPUTTER = new XMLOutputter();
  private static final Logger log =
      LoggerFactory.getLogger(ProductYmlImporter.class);

  private final Repo repo;
  private final Products products;
  private final Tariffs tariffs;

  @Inject
  public ProductYmlImporter(Repo repo, Products products,
                            Tariffs tariffs) {
    this.repo = repo;
    this.products = products;
    this.tariffs = tariffs;
  }

  @Transactional
  public void doImport(Document document, Long parentOfferId,
                       ProductRater rater) {
    log.info("Entring YML import for offer: {}.", parentOfferId);
    com.heymoose.domain.offer.Offer parentOffer =
        repo.get(com.heymoose.domain.offer.Offer.class, parentOfferId);
    Preconditions.checkNotNull(parentOffer, "Offer does not exist.");
    parentOffer.setIsProductOffer(true);
    repo.put(parentOffer);

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

    repo.session().flush();
    repo.session().clear();

    // importing products
    products.deactivateAll(parentOfferId);
    for (Element offer : listOffers(document)) {
      try {
        Product product = new Product();
        product.setName(extractTitle(offer))
            .setOffer(parentOffer)
            // groupId for trendsbrands
            .setOriginalId(extractOriginalId(offer))
            .setPrice(new BigDecimal(offer.getChildText("price")))
            .setUrl(offer.getChildText("url"));
        for (Attribute offerAttribute : offer.getAttributes()) {
          product.addExtraInfo(
              offerAttribute.getName(),
              offerAttribute.getValue());
        }

        // adding attributes
        ProductAttributeBatch attributeBatchInsert =
            new ProductAttributeBatch(repo.session());
        // importing attributes
        for (Element offerChild : offer.getChildren()) {
          if (offerChild.getName().equals("categoryId")) continue;
          ProductAttribute productAttribute = new ProductAttribute()
              .setProduct(product)
              .setKey(offerChild.getName())
              .setValue(offerChild.getText());
          for (Attribute attr : offerChild.getAttributes()) {
            productAttribute.addExtraInfo(attr.getName(), attr.getValue());
          }
          product.addAttribute(productAttribute);
          attributeBatchInsert.add(productAttribute);
        }

        // adding category mappings
        ProductCategoryBatch categoryMappingBatchInsert =
            new ProductCategoryBatch(repo.session());
        for (Element categoryIdElement : offer.getChildren("categoryId")) {
          ShopCategory category = categoryMap.get(categoryIdElement.getText());
          if (category == null) continue; // malformed yml
          ProductCategoryMapping mapping = new ProductCategoryMapping()
              .setCategory(category)
              .setProduct(product);
          categoryMappingBatchInsert.add(mapping);
          product.addCategoryMapping(mapping);
          while (category.parent() != null) {
            category = category.parent();
            mapping = new ProductCategoryMapping()
                .setCategory(category)
                .setProduct(product)
                .isNotDirect();
            categoryMappingBatchInsert.add(mapping);
            product.addCategoryMapping(mapping);
          }
        }

        try {
          // rater needs fully filled product!
          Tariff tariff = rater.rate(product);
          tariff = tariffs.createIfNotExists(tariff);
          product.setTariff(tariff);
        } catch (NoInfoException e) {
          log.info("No pricing info found for product: {}", product);
        }
        saveOrUpdateProduct(product);
        // this should be flushed after saving product because of product id
        products.clearAttributes(product);
        attributeBatchInsert.flush();
        products.clearCategories(product);
        categoryMappingBatchInsert.flush();
      } catch (RuntimeException e) {
        log.warn("Error importing product. Skipping..:\n{}.",
            OUTPUTTER.outputString(offer));
        log.error("Error importing product: ", e);
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

  private void saveOrUpdateProduct(Product product) {
    log.info("Saving product: {}", product);
    repo.session().doWork(new SaveOrUpdateProductWork(product));
  }

  private String attrCoalesce(Element element, String key1, String... keys) {
    String value = element.getAttributeValue(key1);
    int i = 0;
    while (value == null && i < keys.length) {
      value = element.getAttributeValue(keys[i++]);
    }
    return value;
  }

  private String extractOriginalId(Element offer) {
    return attrCoalesce(offer, "id", "group_id");
  }

  private String extractTitle(Element offer) {
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
