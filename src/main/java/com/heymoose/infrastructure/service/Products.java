package com.heymoose.infrastructure.service;

import com.google.common.base.Strings;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.infrastructure.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;

import javax.inject.Inject;
import java.util.List;

public class Products {

  private static final int ITEMS_PER_PAGE = 100;

  private final Repo repo;

  @Inject
  public Products(Repo repo) {
    this.repo = repo;
  }


  @SuppressWarnings("unchecked")
  public Iterable<Product> list(List<Long> offerList,
                                List<Long> categoryList,
                                String queryString,
                                int page) {
    Criteria criteria = repo.session().createCriteria(Product.class);
    if (!offerList.isEmpty()) {
      criteria.createAlias("offer", "offer");
      criteria.add(Restrictions.in("offer.id", offerList));
    }
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
}
