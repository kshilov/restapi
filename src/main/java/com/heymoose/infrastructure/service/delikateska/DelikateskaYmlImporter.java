package com.heymoose.infrastructure.service.delikateska;

import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlImporter;

import java.math.BigDecimal;
import java.util.Map;

public final class DelikateskaYmlImporter extends YmlImporter {

  private static final BigDecimal DEFAULT_PERCENT = new BigDecimal(10);

  private final Map<Integer, Integer> idPercentMap;

  public DelikateskaYmlImporter(Repo repo, Map<Integer, Integer> idPercentMap) {
    super(repo);
    this.idPercentMap = idPercentMap;
  }

  @Override
  protected BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog) {
    Integer offerId = Integer.valueOf(catalogOffer.getId());
    if (idPercentMap.containsKey(offerId))
      return new BigDecimal(idPercentMap.get(offerId));
    return DEFAULT_PERCENT;
  }

  @Override
  protected boolean isExclusive(Offer catalogOffer, YmlCatalog catalog) {
    return idPercentMap.containsKey(Integer.valueOf(catalogOffer.getId()));
  }
}
