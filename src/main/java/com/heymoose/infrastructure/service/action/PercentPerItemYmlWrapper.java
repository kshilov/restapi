package com.heymoose.infrastructure.service.action;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.NoInfoException;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;

import java.math.BigDecimal;
import java.util.Map;

public final class PercentPerItemYmlWrapper extends YmlCatalogWrapperBase {


  private final Map<String, BigDecimal> idPercentMap;
  private final BigDecimal defaultPercent;

  public PercentPerItemYmlWrapper(BigDecimal defaultPercent,
                                  Map<String, BigDecimal> idPercentMap) {
    this.defaultPercent = defaultPercent;
    this.idPercentMap = idPercentMap;
  }

  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    return CpaPolicy.PERCENT;
  }

  @Override
  public BigDecimal getPercent(Offer catalogOffer) {
    String offerId = catalogOffer.getId();
    if (idPercentMap.containsKey(offerId))
      return idPercentMap.get(offerId);
    return defaultPercent;
  }

  @Override
  public BigDecimal getCost(Offer catalogOffer) throws NoInfoException {
    return null;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer) {
    return idPercentMap.containsKey(catalogOffer.getId());
  }
}
