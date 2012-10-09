package com.heymoose.infrastructure.service.topshop;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Category;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class TopShopYmlWrapper extends YmlCatalogWrapperBase {

  private static final Logger log = LoggerFactory
      .getLogger(TopShopYmlWrapper.class);

  private static final BigDecimal TWO = new BigDecimal(2);
  private static final BigDecimal SIX = new BigDecimal(6);
  private static final BigDecimal EIGHT = new BigDecimal(8);
  private static final BigDecimal FIFTEEN = new BigDecimal(15);
  private static final Map<Integer, BigDecimal> CATEGORY_PERCENT_MAP =
      ImmutableMap.<Integer, BigDecimal>builder()
          .put(12, FIFTEEN) // тв товары
          .put(17, FIFTEEN) // дормео
          .put(19, FIFTEEN) // космодиск
          .put(18, FIFTEEN) // делимано
          .put(1, TWO)      // бытовая техника
          .put(2, TWO)      // электроника
          .put(3, TWO)      // товары для дома
          .put(4, SIX)      // дача, сад, огород
          .put(5, SIX)      // красота и здоровье
          .put(9, EIGHT)    // спорт и отдых
          .put(6, EIGHT)    // товары для детей
          .put(7, EIGHT)    // товары для автомобиля
          .put(8, SIX)      // одежда и обувь
          .put(10, SIX)     // подарки, украшения
          .build();
  private static final Set<Integer> EXCLUSIVE_CATEGORIES =
      ImmutableSet.of(12, 17, 18, 19);


  private Map<Integer, Integer> childToParentCategory;

  @Override
  public YmlCatalogWrapperBase wrapCatalog(YmlCatalog catalog) {
    super.wrapCatalog(catalog);
    mapChildToParent();
    return this;
  }

  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    return CpaPolicy.PERCENT;
  }

  @Override
  public BigDecimal getPercent(Offer catalogOffer) throws NoInfoException {
    Integer parentCategory = getParentCategory(catalogOffer);
    if (!CATEGORY_PERCENT_MAP.containsKey(parentCategory)) {
      log.warn("Category {} is not mapped. Skipping.", parentCategory);
      throw new NoInfoException("Category unknown" + parentCategory);
    }
    return CATEGORY_PERCENT_MAP.get(parentCategory);
  }

  @Override
  public boolean isExclusive(Offer catalogOffer) throws NoInfoException {
    return EXCLUSIVE_CATEGORIES.contains(getParentCategory(catalogOffer));
  }

  private Integer getParentCategory(Offer catalogOffer)
      throws NoInfoException {
    String productCategoryString = catalogOffer.getCategoryId().get(0)
        .getvalue();
    String productName = getOfferTitle(catalogOffer);
    if (Strings.isNullOrEmpty(productCategoryString)) {
      log.warn("Category does not present for product {} - {}. Skipping.",
          catalogOffer.getId(), productName);
      throw new NoInfoException("No category for product "
          + catalogOffer.getId());
    }
    Integer productCategory = Integer.valueOf(productCategoryString);
    Integer parentCategory = childToParentCategory.get(productCategory);
    if (parentCategory == null) {
      parentCategory = productCategory;
    }
    return parentCategory;
  }

  private void mapChildToParent() {
    HashMap<Integer, Integer> parentMap = Maps.newHashMap();
    for (Category category : catalog.getShop().getCategories().getCategory()) {
      Integer parentId = null;
      if (category.getParentId() != null) {
        parentId = Integer.valueOf(category.getParentId());
      }
      parentMap.put(Integer.valueOf(category.getId()), parentId);
    }
    boolean changed = true;
    // map child to the top level parent
    outer:
    while (changed) {
      for (Integer categoryId : parentMap.keySet()) {
        Integer parentId = parentMap.get(categoryId);
        // if parent of the category has parents itself
        if (parentId != null && parentMap.get(parentId) != null) {
          parentMap.put(categoryId, parentMap.get(parentId));
          continue outer;
        }
      }
      changed = false;
    }
    this.childToParentCategory = parentMap;
  }


}
