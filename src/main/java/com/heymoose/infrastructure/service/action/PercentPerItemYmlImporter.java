package com.heymoose.infrastructure.service.action;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlImporter;

import java.math.BigDecimal;
import java.util.Map;

public class PercentPerItemYmlImporter extends YmlImporter {

  private final Map<String, BigDecimal> idPercentMap;
  private final BigDecimal defaultPercent;

  @Inject
  public PercentPerItemYmlImporter(Repo repo,
                                   @Named("default-percent")
                                   BigDecimal defaultPercent,
                                   @Named("id-percent-map")
                                   Map<String, BigDecimal> idPercentMap) {
    super(repo);
    this.idPercentMap = idPercentMap;
    this.defaultPercent = defaultPercent;
  }

  @Override
  protected CpaPolicy getCpaPolicy(Offer catalogOffer, YmlCatalog catalog) {
    return CpaPolicy.PERCENT;
  }

  @Override
  protected BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog) {
    String offerId = catalogOffer.getId();
    if (idPercentMap.containsKey(offerId))
      return idPercentMap.get(offerId);
    return defaultPercent;
  }

  @Override
  protected BigDecimal getCost(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    return null;
  }

  @Override
  protected boolean isExclusive(Offer catalogOffer, YmlCatalog catalog) {
    return idPercentMap.containsKey(catalogOffer.getId());
  }
}
