package com.heymoose.infrastructure.service.trendsbrands;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.TypePrefix;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlImporter;

import java.math.BigDecimal;

public final class TrendsBrandsYmlImporter extends YmlImporter {

  private static final BigDecimal EXCLUSIVE_LIMIT = new BigDecimal(6000);
  private static final BigDecimal EXCLUSIVE_COST = new BigDecimal(1950);
  private static final BigDecimal REGULAR_COST = new BigDecimal(650);

  public TrendsBrandsYmlImporter(Repo repo) {
    super(repo);
  }

  @Override
  protected CpaPolicy getCpaPolicy(Offer catalogOffer, YmlCatalog catalog) {
    return CpaPolicy.FIXED;
  }

  @Override
  protected BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    return null;
  }

  @Override
  protected BigDecimal getCost(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    BigDecimal price = new BigDecimal(catalogOffer.getPrice());
    if (isExclusive(catalogOffer, catalog)) {
      return EXCLUSIVE_COST;
    }
    return REGULAR_COST;
  }

  @Override
  protected boolean isExclusive(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    BigDecimal price = new BigDecimal(catalogOffer.getPrice());
    BigDecimal basePrice = new BigDecimal(catalogOffer.getBasePrice());
    return basePrice.compareTo(EXCLUSIVE_LIMIT) > 0 && basePrice.equals(price);
  }

  @Override
  protected String getOfferCode(Offer catalogOffer) {
    return catalogOffer.getGroupId();
  }

  @Override
  protected String getOfferTitle(Offer offer) {
    return titleFor(offer, TypePrefix.class, Vendor.class, Model.class);
  }
}
