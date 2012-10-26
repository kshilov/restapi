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

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

public class Products {

  private static final int ITEMS_PER_PAGE = 100;

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
    criteria.add(Restrictions.in("offer.id", offerList));

    if (!categoryList.isEmpty()) {
      criteria.createAlias("category", "category");
      Criterion categoryMatches = Restrictions.in("category.id", categoryList);
      Criterion parentMatches = HibernateUtil.sqlInRestriction(
          "exists (with recursive children(id, parent_id) as (" +
            "select id, parent_id from shop_category where parent_id is not null " +
            "union " +
            "select children.id, shop_category.parent_id " +
              "from children " +
              "join shop_category on shop_category.id = children.parent_id) " +
          "select * from children " +
              "where parent_id in (?) and id = {alias}.shop_category_id)",
          categoryList, StandardBasicTypes.LONG);
      criteria.add(Restrictions.or(categoryMatches, parentMatches));
    }
    if (!Strings.isNullOrEmpty(queryString)) {
      criteria.add(Restrictions.sqlRestriction(
          "lower({alias}.name) like ?",
          "%" + queryString.toLowerCase() + "%",
          StandardBasicTypes.STRING));
    }
    criteria.setFirstResult(page * ITEMS_PER_PAGE);
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
    repo.session().createQuery("delete from ProductAttribute where product.id = ?")
        .setParameter(0, product.id());
  }

  public void deactivateAll(Long parentOfferId) {
    repo.session().createSQLQuery(
        "update product set active = false " +
            "where offer_id = ?")
        .setParameter(0, parentOfferId)
        .executeUpdate();
  }
}
