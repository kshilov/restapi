package com.heymoose.infrastructure.service;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ProductCategoryMapping;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.util.TypedMap;
import com.heymoose.infrastructure.util.db.QueryResult;
import com.heymoose.infrastructure.util.db.SqlLoader;
import com.heymoose.infrastructure.util.db.TemplateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Products {

  private static class MapToProductFunction
      implements Function<Map<String, Object>, Product> {

    @Override
    public Product apply(@Nullable Map<String, Object> record) {
      if (record == null) return null;
      TypedMap map = TypedMap.wrap(record);
      Product product = new Product()
          .setName(map.getString("product_name"))
          .setPrice(map.getBigDecimal("product_price"))
          .setActive(map.getBoolean("product_active"))
          .setOriginalId(map.getString("product_name"))
          .setUrl(map.getString("product_url"));
      product.setId(map.getLong("product_id"));
      if (map.get("offer_id") != null) {
        Offer offer = new Offer()
            .setId(map.getLong("offer_id"))
            .setName(map.getString("offer_name"));
        product.setOffer(offer);
      }
      if (map.get("tariff_id") != null) {
        Tariff tariff = new Tariff()
            .setPercent(map.getBigDecimal("tariff_percent"))
            .setCost(map.getBigDecimal("tariff_cost"))
            .setFirstActionCost(map.getBigDecimal("tariff_first_action_cost"))
            .setOtherActionCost(map.getBigDecimal("tariff_other_action_cost"))
            .setCpaPolicy(map.getEnumValue("tariff_cpa_policy", CpaPolicy.class))
            .setId(map.getLong("tariff_id"));
        product.setTariff(tariff);
      }
      return product;
    }
  }

  private static class AddAttributesFunction
      implements Function<Product, Product> {
    private Repo repo;

    public AddAttributesFunction(Repo repo) {
      this.repo = repo;
    }

    @Override
    public Product apply(@Nullable Product product) {
      if (product == null) return null;
      QueryResult queryResult = SqlLoader
          .sqlQuery("product-attribute-list", repo.session())
          .addQueryParam("product_id", product.id())
          .execute();
      for (Map<String, Object> raw : queryResult) {
        TypedMap map = TypedMap.wrap(raw);
        ProductAttribute attribute = new ProductAttribute()
            .setProduct(product)
            .setKey(map.getString("key"))
            .setValue(map.getString("value"))
            .setExtraInfo(map.getString("extra_info"));
        product.addAttribute(attribute);
      }
      return product;
    }
  }

  private static class AddCategoriesFunction
      implements Function<Product, Product> {
    private Repo repo;

    public AddCategoriesFunction(Repo repo) {
      this.repo = repo;
    }


    @Override
    public Product apply(@Nullable Product product) {
      if (product == null) return null;
      QueryResult queryResult = SqlLoader
          .sqlQuery("product-category-list", repo.session())
          .addQueryParam("product_id", product.id())
          .execute();
      for (Map<String, Object> raw : queryResult) {
        TypedMap map = TypedMap.wrap(raw);
        ShopCategory category = new ShopCategory()
            .setId(map.getLong("id"))
            .setOfferId(map.getLong("offer_id"))
            .setName(map.getString("name"))
            .setOriginalId(map.getString("original_id"))
            .setParentId(map.getLong("parent_id"));
        ProductCategoryMapping categoryMapping = new ProductCategoryMapping()
            .setProduct(product)
            .setCategory(category)
            .setIsDirect(map.getBoolean("is_direct"));
        product.addCategoryMapping(categoryMapping);
      }
      return product;
    }
  }

  private static final MapToProductFunction MAP_TO_PRODUCT =
      new MapToProductFunction();

  private static final Logger log = LoggerFactory.getLogger(Products.class);

  private final Repo repo;
  private final OfferGrantRepository grants;
  private final AddAttributesFunction addAttributesFunction;
  private final AddCategoriesFunction addCategoriesFunction;

  @Inject
  public Products(Repo repo, OfferGrantRepository grantRepo) {
    this.repo = repo;
    this.grants = grantRepo;
    this.addAttributesFunction = new AddAttributesFunction(repo);
    this.addCategoriesFunction = new AddCategoriesFunction(repo);
  }


  @SuppressWarnings("unchecked")
  public Iterable<Product> list(User user, Site site,
                                Collection<Long> offerList,
                                List<Long> categoryList,
                                String queryString,
                                int offset, Integer limit) {
    TemplateQuery query = productTemplateQuery(
        user, site, offerList, categoryList, queryString);
    query.addTemplateParam("productInfo", true);
    Iterable<Map<String, Object>> mapIterator = query.execute(offset, limit);

    Iterable<Product> productIterator =
        Iterables.transform(mapIterator, MAP_TO_PRODUCT);
    Iterable<Product> attributesAdded =
        Iterables.transform(productIterator, addAttributesFunction);
    return Iterables.transform(attributesAdded, addCategoriesFunction);
  }

  public Long count(User user,
                    Site site,
                    List<Long> offerList,
                    List<Long> categoryList,
                    String queryString) {
    TemplateQuery query = productTemplateQuery(
        user, site, offerList, categoryList, queryString);
    query.addTemplateParam("productInfo", true);
    return query.count();
  }



  public Iterable<ShopCategory> categoryList(User user,
                                             Site site,
                                             List<Long> offerList,
                                             List<Long> categoryList,
                                             String queryString) {
    TemplateQuery query = productTemplateQuery(
        user, site, offerList, categoryList, queryString);
    query.addTemplateParam("categoryInfo", true);
    QueryResult result = query.execute();
    ImmutableList.Builder<ShopCategory> categoryResultList =
        ImmutableList.builder();
    for (Map<String, Object> raw : result) {
      TypedMap map = TypedMap.wrap(raw);
      ShopCategory shopCategory = new ShopCategory()
          .setId(map.getLong("id"))
          .setName(map.getString("name"))
          .setOriginalId(map.getString("original_id"))
          .setParentId(map.getLong("parent_id"))
          .setOfferId(map.getLong("offer_id"));
      categoryResultList.add(shopCategory);
    }
    return categoryResultList.build();
  }


  public List<ShopCategory> categoryList(Long offerId) {
    return repo.allByHQL(ShopCategory.class,
        "from ShopCategory where offerId = ?", offerId);
  }

  public ShopCategory categoryByOriginalId(Long parentOfferId,
                                           String originalId) {

    return repo.byHQL(ShopCategory.class,
        "from ShopCategory where offerId = ? and originalId = ?",
        parentOfferId, originalId);
  }

  public Product byOriginalId(Long parentOfferId,
                              String originalId) {
    return repo.byHQL(Product.class,
          "from Product where offer.id = ? and originalId = ?",
          parentOfferId, originalId);
  }

  public void clearAttributes(Product product) {
    int deleted = repo.session()
        .createQuery("delete from ProductAttribute where product.id = ?")
        .setParameter(0, product.id())
        .executeUpdate();
    log.debug("Deleted {} attributes of product {}", deleted, product);
  }

  public void deactivateAll(Long parentOfferId) {
    int updated = repo.session().createSQLQuery(
        "update product set active = false " +
            "where offer_id = ?")
        .setParameter(0, parentOfferId)
        .executeUpdate();
    log.debug("Deactivated {} products of offer {}", updated, parentOfferId);
  }

  public void clearCategories(Product product) {
    int deleted = repo.session()
        .createSQLQuery("delete from product_category " +
            "where product_id = ?")
        .setParameter(0, product.id())
        .executeUpdate();
    log.debug("Deleted {} product - category mappings of {}", deleted, product);
  }

  private TemplateQuery productTemplateQuery(User user,
                                             Site site,
                                             Collection<Long> offerList,
                                             Collection<Long> categoryList,
                                             String queryString) {
    TemplateQuery query = SqlLoader
        .templateQuery("product-list", repo.session())
        .addTemplateParamIfNotNull(site, "filterBySite", true)
        .addQueryParamIfNotNull(site, "site_id", site.id());

    if (!Strings.isNullOrEmpty(queryString)) {
      query.addTemplateParam("filterByName", true);
    }
    if (!offerList.isEmpty() && !categoryList.isEmpty()) {
      query.addTemplateParam("filterByOfferListAndCategoryList", true);
      query.addQueryParam("offer_list", offerList);
      query.addQueryParam("category_list", categoryList);
    } else {
      if (!offerList.isEmpty()) {
        query.addTemplateParam("filterByOfferList", true);
        query.addQueryParam("offer_list", offerList);
      }
      if (!categoryList.isEmpty()) {
        query.addTemplateParam("filterByCategoryList", true);
        query.addQueryParam("category_list", categoryList);
      }
    }
    return query.addQueryParam("user_id", user.id());
  }

}
