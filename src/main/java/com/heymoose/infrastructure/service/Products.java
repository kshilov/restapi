package com.heymoose.infrastructure.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.user.User;
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
import java.util.Map;

public class Products {

  private static final int ITEMS_PER_PAGE = 100;
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
    Criteria criteria = repo.session().createCriteria(Product.class)
        .add(Restrictions.eq("active", true));
    criteria.createAlias("offer", "offer");

    Criterion shopMatches = Restrictions.in("offer.id", offerList);

    Criterion categoryMatches = HibernateUtil.sqlInRestriction(
          "exists (select * from product_category " +
              "where product_id = {alias}.id and shop_category_id in (?))",
          categoryList, StandardBasicTypes.LONG);

    if (offerList.isEmpty() || categoryList.isEmpty()) {
      if (!offerList.isEmpty()) criteria.add(shopMatches);
      if (!categoryList.isEmpty()) criteria.add(categoryMatches);
    } else {
      criteria.add(Restrictions.or(categoryMatches, shopMatches));
    }

    Map<Long, Offer> grantedMap =
        IdEntity.toMap(grants.grantedProductOffers(user.id()));
    if (grantedMap.size() == 0) return ImmutableList.of();
    criteria.add(Restrictions.in("offer.id", grantedMap.keySet()));

    if (!Strings.isNullOrEmpty(queryString)) {
      criteria.add(Restrictions.sqlRestriction(
          "lower({alias}.name) like ?",
          "%" + queryString.toLowerCase() + "%",
          StandardBasicTypes.STRING));
    }
    criteria.setFirstResult(offset);
    if (limit != null) criteria.setMaxResults(limit);
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
