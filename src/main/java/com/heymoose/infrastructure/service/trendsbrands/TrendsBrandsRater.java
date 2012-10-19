package com.heymoose.infrastructure.service.trendsbrands;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class TrendsBrandsRater implements ProductRater {

  private static final BigDecimal EXCLUSIVE_LIMIT = new BigDecimal(6000);
  private static final BigDecimal EXCLUSIVE_COST = new BigDecimal(1950);
  private static final BigDecimal REGULAR_COST = new BigDecimal(650);

  @Override
  public Tariff rate(Product product) {
    Tariff tariff = Tariff.forProduct(product).setCpaPolicy(CpaPolicy.FIXED);
    boolean isExclusive;
    BigDecimal price = product.price();
    BigDecimal basePrice = new BigDecimal(product.attributeValue("baseprice"));
    isExclusive = basePrice.compareTo(EXCLUSIVE_LIMIT) > 0 &&
        basePrice.equals(price);
    product.setExclusive(isExclusive);
    if (isExclusive) tariff.setCost(EXCLUSIVE_COST);
    else tariff.setCost(REGULAR_COST);
    return tariff;
  }
}
