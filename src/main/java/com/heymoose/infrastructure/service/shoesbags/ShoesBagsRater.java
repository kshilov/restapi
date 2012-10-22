package com.heymoose.infrastructure.service.shoesbags;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;
import java.util.List;

public final class ShoesBagsRater implements ProductRater {

  private static final List<String> EXCLUSIVE_VENDORS =
      ImmutableList.of(
          "karma of charme",
          "baldinini",
          "alexander hotto",
          "norma j.baker",
          "renato angi",
          "loriblu",
          "shy");
  private static final BigDecimal REGULAR_PERCENT = new BigDecimal(15);
  private static final BigDecimal EXCLUSIVE_COST = new BigDecimal(1950);

  @Override
  public Tariff rate(Product product) {
    Tariff tariff = Tariff.forProduct(product);
    String vendor = product.attributeValue("vendor").toLowerCase();
    boolean isExclusive = EXCLUSIVE_VENDORS.contains(vendor);
    tariff.setExclusive(isExclusive);
    if (isExclusive)
      tariff.setCpaPolicy(CpaPolicy.FIXED).setCost(EXCLUSIVE_COST);
    else
      tariff.setCpaPolicy(CpaPolicy.PERCENT).setPercent(REGULAR_PERCENT);
    return tariff;
  }
}
