package com.heymoose.infrastructure.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.infrastructure.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

public class Products {

  private static final int ITEMS_PER_PAGE = 100;
  private static final Logger log = LoggerFactory.getLogger(Products.class);

  private final Repo repo;

  @Inject
  public Products(Repo repo) {
    this.repo = repo;
  }


  @SuppressWarnings("unchecked")
  public Iterable<Product> list(Collection<Long> offerList,
                                List<Long> categoryList,
                                String queryString,
                                int page) {
    if (offerList.isEmpty()) return ImmutableList.of();

    Criteria criteria = repo.session().createCriteria(Product.class)
        .add(Restrictions.eq("active", true));
    criteria.createAlias("offer", "offer");

    Criterion shopOrCategoryMatches = null;
    Criterion shopMatches = Restrictions.in("offer.id", offerList);

    if (!categoryList.isEmpty()) {
      Criterion categoryMatches = HibernateUtil.sqlInRestriction(
          "exists (select * from product_category " +
              "where product_id = {alias}.id and shop_category_id in (?))",
          categoryList, StandardBasicTypes.LONG);
      shopOrCategoryMatches = Restrictions.or(shopMatches, categoryMatches);
    } else {
      shopOrCategoryMatches = shopMatches;
    }
    if (!Strings.isNullOrEmpty(queryString)) {
      criteria.add(Restrictions.sqlRestriction(
          "lower({alias}.name) like ?",
          "%" + queryString.toLowerCase() + "%",
          StandardBasicTypes.STRING));
    }
    criteria.add(shopOrCategoryMatches);
    criteria.setFirstResult((page - 1) * ITEMS_PER_PAGE);
    criteria.setMaxResults(ITEMS_PER_PAGE);
    return (List<Product>) criteria.list();
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
}
