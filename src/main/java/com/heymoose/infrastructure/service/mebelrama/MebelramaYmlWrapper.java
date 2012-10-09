package com.heymoose.infrastructure.service.mebelrama;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;

import java.math.BigDecimal;

public final class MebelramaYmlWrapper extends YmlCatalogWrapperBase {

  private static final BigDecimal EXCLUSIVE_LIMIT = new BigDecimal(15000);
  private static final BigDecimal EXCLUSIVE_PERCENT = new BigDecimal(11);
  private static final BigDecimal DEFAULT_PERCENT = new BigDecimal(10);

  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    return CpaPolicy.PERCENT;
  }

  @Override
  public BigDecimal getPercent(Offer catalogOffer)
      throws NoInfoException {
    if (isExclusive(catalogOffer)) {
      return EXCLUSIVE_PERCENT;
    }
    return DEFAULT_PERCENT;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer)
      throws NoInfoException {
    return new BigDecimal(catalogOffer.getPrice())
        .compareTo(EXCLUSIVE_LIMIT) > 0;
  }
}
