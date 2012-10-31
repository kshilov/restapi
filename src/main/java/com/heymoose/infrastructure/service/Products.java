package com.heymoose.infrastructure.service;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.util.ProviderWithSetter;
import com.heymoose.infrastructure.util.SqlLoader;
import com.heymoose.resource.TypedMap;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

  private static final MapToProductFunction MAP_TO_PRODUCT =
      new MapToProductFunction();

  private static final Logger log = LoggerFactory.getLogger(Products.class);

  private final Repo repo;
  private final OfferGrantRepository grants;

  @Inject
  public Products(Repo repo, OfferGrantRepository grantRepo) {
    this.repo = repo;
    this.grants = grantRepo;
  }


  @SuppressWarnings("unchecked")
  public Iterable<Product> list(User user, Collection<Long> offerList,
                                List<Long> categoryList,
                                String queryString,
                                int offset, Integer limit) {
    Iterable<Map<String, Object>> mapIterator =
        listProducts(user, offerList, categoryList, queryString);
    Iterable<Product> productIterator =
        Iterables.transform(mapIterator, MAP_TO_PRODUCT);
//    Iterable<Product> filledCategoriesIterator =
//        Iterables.transform(productIterator, FILL_CATEGORIES);
//    Iterable<Product> filledAttributesIterator =
//        Iterables.transform(filledCategoriesIterator, FILL_ATTRIBUTES);
    return productIterator;

//    Criteria criteria = repo.session().createCriteria(Product.class)
//        .add(Restrictions.eq("active", true));
//    criteria.createAlias("offer", "offer");
//
//    Criterion shopMatches = Restrictions.in("offer.id", offerList);
//
//    Criterion categoryMatches = HibernateUtil.sqlInRestriction(
//          "exists (select * from product_category " +
//              "where product_id = {alias}.id and shop_category_id in (?))",
//          categoryList, StandardBasicTypes.LONG);
//
//    if (offerList.isEmpty() || categoryList.isEmpty()) {
//      if (!offerList.isEmpty()) criteria.add(shopMatches);
//      if (!categoryList.isEmpty()) criteria.add(categoryMatches);
//    } else {
//      criteria.add(Restrictions.or(categoryMatches, shopMatches));
//    }
//
//    Map<Long, Offer> grantedMap =
//        IdEntity.toMap(grants.grantedProductOffers(user.id()));
//    if (grantedMap.size() == 0) return ImmutableList.of();
//    criteria.add(Restrictions.in("offer.id", grantedMap.keySet()));
//
//    if (!Strings.isNullOrEmpty(queryString)) {
//      criteria.add(Restrictions.sqlRestriction(
//          "lower({alias}.name) like ?",
//          "%" + queryString.toLowerCase() + "%",
//          StandardBasicTypes.STRING));
//    }
//    criteria.setFirstResult(offset);
//    if (limit != null) criteria.setMaxResults(limit);
//    return (List<Product>) criteria.list();
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

  public Product productByOriginalId(Long parentOfferId,
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

  public Iterable<Map<String, Object>> listProducts(final User user,
                                                    final Iterable<Long> offerList,
                                                    final Iterable<Long> categoryList,
                                                    final String queryString) {
    ImmutableMap.Builder<String, Object> templateParams =
        ImmutableMap.builder();
    if (!Strings.isNullOrEmpty(queryString)) {
      templateParams.put("filterByName", true);
    }
    if (offerList.iterator().hasNext()) {
      templateParams.put("offerList", offerList);
    }
    if (categoryList.iterator().hasNext()) {
      templateParams.put("categoryList", categoryList);
    }
    final String sql = SqlLoader.getTemplate("product-list",
        templateParams.build());

    final ProviderWithSetter<Iterable<Map<String, Object>>> provider =
        ProviderWithSetter.newInstance();
    repo.session().doWork(new Work() {
      @Override
      public void execute(Connection connection) throws SQLException {
        SqlLoader.NamedParameterStatement statement = SqlLoader
            .NamedParameterStatement.create(sql, connection);
        statement.setLong("user_id", user.id());
        if (!Strings.isNullOrEmpty(queryString)) {
          statement.setString("query_string", queryString.toLowerCase());
        }
        statement.setInLong("offer_id", offerList);
        statement.setInLong("category_id", categoryList);
        ResultSet resultSet = statement.executeQuery();
        provider.set(SqlLoader.toIterable(resultSet));
      }
    });
    return provider.get();
  }

}
