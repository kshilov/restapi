package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductRater;
import com.heymoose.domain.tariff.Tariff;

import java.math.BigDecimal;
import java.util.Map;

public final class MapRater implements ProductRater {

  private final Map<String, BigDecimal> idPercent;
  private final BigDecimal defaultPercent;

  public MapRater(Map<String, BigDecimal> idPercent,
                  BigDecimal defaultPercent) {
    this.idPercent = idPercent;
    this.defaultPercent = defaultPercent;
  }

  @Override
  public Tariff rate(Product product) throws NoInfoException {
    Tariff tariff = Tariff.forProduct(product).setCpaPolicy(CpaPolicy.PERCENT);
    if (idPercent.containsKey(product.originalId())) {
      tariff.setExclusive(true);
      return tariff.setPercent(idPercent.get(product.originalId()));
    }
    return tariff.setPercent(defaultPercent);
  }
}
