package com.heymoose.infrastructure.service.yml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.service.Tariffs;
import com.heymoose.infrastructure.util.BatchQuery;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;

@Singleton
public class ProductYmlImporter {

  private static final class ProductAttributeBatch
      extends BatchQuery<ProductAttribute> {

    private static final String SQL = "insert into product_attribute " +
        "(product_id, key, value, extra_info) values (?, ?, ?, ?)";
    private static final int BATCH_SIZE = 100;

    public ProductAttributeBatch(Session session) {
      super(BATCH_SIZE, session, SQL);
    }


    @Override
    protected void transform(ProductAttribute item, PreparedStatement statement)
        throws SQLException {
      int i = 1;
      statement.setLong(i++, item.product().id());
      statement.setString(i++, item.key());
      statement.setString(i++, item.value());
      statement.setString(i++, item.extraInfoString());
    }
  }

  private static final class SaveProductWork implements Work {

    private static final String sql = "insert into product " +
        "(shop_category_id, offer_id, tariff_id, name, url, original_id, price) " +
        "values (?, ?, ?, ?, ?, ?, ?) " +
        "returning id";

    private final Product product;

    private SaveProductWork(Product product) {
      this.product = product;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
      PreparedStatement statement = connection.prepareStatement(sql);
      int i = 1;
      if (product.category() == null) {
        statement.setNull(i++, Types.BIGINT);
      } else {
        statement.setLong(i++, product.category().id());
      }
      statement.setLong(i++, product.offer().id());
      if (product.tariff() != null) {
        statement.setLong(i++, product.tariff().id());
      } else {
        statement.setNull(i++, Types.BIGINT);
      }
      statement.setString(i++, product.name());
      statement.setString(i++, product.url());
      statement.setString(i++, product.originalId());
      statement.setBigDecimal(i++, product.price());
      statement.execute();
      ResultSet keys = statement.getResultSet();
      if (keys.next()) product.setId(keys.getLong(1));
    }
  }

  private static final Splitter DOT = Splitter.on('.');
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
    // importing products
    ProductAttributeBatch attributeBatchInsert =
        new ProductAttributeBatch(repo.session());
    for (Element offer : listOffers(document)) {
      try {
        String originalId = offer.getAttributeValue("id");
  //      Product product = products.productByOriginalId(parentOfferId, originalId);
  //      if (product == null) product = new Product();
        Product product = new Product();
        String categoryOriginalId = offer.getChildText("categoryId");
        product.setCategory(categoryMap.get(categoryOriginalId))
            .setName(extractTitle(offer))
            .setOffer(parentOffer)
            // groupId for trendsbrands
            .setOriginalId(extractOriginalId(offer))
            .setPrice(new BigDecimal(offer.getChildText("price")))
            .setUrl(offer.getChildText("url"));
        // importing attributes
        for (Element offerChild : offer.getChildren()) {
          products.clearAttributes(product);
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
        try {
          Tariff tariff = rater.rate(product);
          tariff = tariffs.createIfNotExists(tariff);
          product.setTariff(tariff);
        } catch (NoInfoException e) {
          log.info("No pricing info found for product: {}", product);
        }
        save(product);
        attributeBatchInsert.flush();
      } catch (RuntimeException e) {
        log.warn("Error importing product: {}. Skipping..",
            new XMLOutputter().outputString(offer));
        log.error("Error importing product: ", e);
      }
    }
    attributeBatchInsert.flush();
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

  private void save(Product product) {
    log.info("Saving product: {}", product);
    repo.session().doWork(new SaveProductWork(product));
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
