package com.heymoose.infrastructure.service.shoesbags;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;
import com.heymoose.infrastructure.service.yml.YmlUtil;

import java.math.BigDecimal;
import java.util.List;

public final class ShoesBagsYmlWrapper extends YmlCatalogWrapperBase {


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


  public ShoesBagsYmlWrapper(YmlCatalog catalog) {
    super(catalog);
  }

  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    boolean isExclusive = false;
    try {
      isExclusive = isExclusive(catalogOffer);
    } catch (NoInfoException e) {
      // isExclusive = false;
    }
    if (isExclusive) {
      return CpaPolicy.FIXED;
    }
    return CpaPolicy.PERCENT;
  }

  @Override
  public BigDecimal getPercent(Offer catalogOffer) throws NoInfoException {
    if (isExclusive(catalogOffer)) {
      return null;
    }
    return REGULAR_PERCENT;
  }

  @Override
  public BigDecimal getCost(Offer catalogOffer) throws NoInfoException {
    if (isExclusive(catalogOffer)) {
      return EXCLUSIVE_COST;
    }
    return null;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer)
      throws NoInfoException {
    return EXCLUSIVE_VENDORS.contains(
        YmlUtil.extractOptionalField(catalogOffer, Vendor.class).toLowerCase());
  }

  @Override
  public String getOfferTitle(Offer offer) {
    return YmlUtil.titleFor(offer, Vendor.class, Model.class);
  }
}
