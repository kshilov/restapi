package com.heymoose.infrastructure.service.topshop;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.service.yml.NoInfoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class TopShopRater {


  private static final Logger log = LoggerFactory
      .getLogger(TopShopRater.class);

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

  public Tariff rate(Product product) throws NoInfoException {
    Tariff tariff = Tariff.forProduct(product).setCpaPolicy(CpaPolicy.PERCENT);
    BigDecimal percent = null;
    ShopCategory category = product.category();
    while (percent == null && category != null) {
      Integer originalId = Integer.valueOf(category.originalId());
      percent = CATEGORY_PERCENT_MAP.get(originalId);
      if (EXCLUSIVE_CATEGORIES.contains(originalId)) product.setExclusive(true);
      category = category.parent();
    }
    if (percent == null) throw new NoInfoException(
        "No price info for product: " + product);
    tariff.setPercent(percent);
    return tariff;
  }
}
