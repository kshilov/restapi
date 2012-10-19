package com.heymoose.infrastructure.service.mebelrama;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;

public final class MebelramaRater implements ProductRater {

  private static final BigDecimal EXCLUSIVE_LIMIT = new BigDecimal(15000);
  private static final BigDecimal EXCLUSIVE_PERCENT = new BigDecimal(11);
  private static final BigDecimal DEFAULT_PERCENT = new BigDecimal(10);

  @Override
  public Tariff rate(Product product) {
    Tariff tariff = Tariff.forProduct(product).setCpaPolicy(CpaPolicy.PERCENT);
    boolean isExclusive = product.price().compareTo(EXCLUSIVE_LIMIT) > 0;
    product.setExclusive(isExclusive);
    if (isExclusive) tariff.setPercent(EXCLUSIVE_PERCENT);
    else tariff.setPercent(DEFAULT_PERCENT);
    return tariff;
  }
}
