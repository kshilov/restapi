package com.heymoose.infrastructure.service.shoesbags;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlImporter;

import java.math.BigDecimal;
import java.util.List;

public final class ShoesBagsYmlImporter extends YmlImporter {

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


  public ShoesBagsYmlImporter(Repo repo) {
    super(repo);
  }

  @Override
  protected CpaPolicy getCpaPolicy(Offer catalogOffer, YmlCatalog catalog) {
    boolean isExclusive = false;
    try {
      isExclusive = isExclusive(catalogOffer, catalog);
    } catch (NoInfoException e) {
      // isExclusive = false;
    }
    if (isExclusive) {
      return CpaPolicy.FIXED;
    }
    return CpaPolicy.PERCENT;
  }

  @Override
  protected BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    if (isExclusive(catalogOffer, catalog)) {
      return null;
    }
    return REGULAR_PERCENT;
  }

  @Override
  protected BigDecimal getCost(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    if (isExclusive(catalogOffer, catalog)) {
      return EXCLUSIVE_COST;
    }
    return null;
  }

  @Override
  protected boolean isExclusive(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    return EXCLUSIVE_VENDORS.contains(
        extractOptionalField(catalogOffer, Vendor.class).toLowerCase());
  }

  @Override
  protected String getOfferTitle(Offer offer) {
    return titleFor(offer, Vendor.class, Model.class);
  }
}
