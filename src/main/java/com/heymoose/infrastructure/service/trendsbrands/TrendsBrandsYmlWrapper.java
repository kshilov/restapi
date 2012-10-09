package com.heymoose.infrastructure.service.trendsbrands;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.TypePrefix;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;
import com.heymoose.infrastructure.service.yml.YmlUtil;

import java.math.BigDecimal;

public final class TrendsBrandsYmlWrapper extends YmlCatalogWrapperBase {

  private static final BigDecimal EXCLUSIVE_LIMIT = new BigDecimal(6000);
  private static final BigDecimal EXCLUSIVE_COST = new BigDecimal(1950);
  private static final BigDecimal REGULAR_COST = new BigDecimal(650);

  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    return CpaPolicy.FIXED;
  }

  @Override
  public BigDecimal getPercent(Offer catalogOffer) throws NoInfoException {
    return null;
  }

  @Override
  public BigDecimal getCost(Offer catalogOffer) throws NoInfoException {
    BigDecimal price = new BigDecimal(catalogOffer.getPrice());
    if (isExclusive(catalogOffer)) {
      return EXCLUSIVE_COST;
    }
    return REGULAR_COST;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer) throws NoInfoException {
    BigDecimal price = new BigDecimal(catalogOffer.getPrice());
    BigDecimal basePrice = new BigDecimal(catalogOffer.getBasePrice());
    return basePrice.compareTo(EXCLUSIVE_LIMIT) > 0 && basePrice.equals(price);
  }

  @Override
  public String getOfferCode(Offer catalogOffer) {
    return catalogOffer.getGroupId();
  }

  @Override
  public String getOfferTitle(Offer offer) {
    return YmlUtil.titleFor(offer, TypePrefix.class, Vendor.class, Model.class);
  }
}
