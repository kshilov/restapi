package com.heymoose.infrastructure.service.carolines;

import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapperBase;
import com.heymoose.infrastructure.service.yml.YmlUtil;

import java.math.BigDecimal;
import java.util.List;

public final class CarolinesYmlWrapper extends YmlCatalogWrapperBase {

  public static final BigDecimal EXCLUSIVE_COST = new BigDecimal(300);
  public static final BigDecimal REGULAR_COST = new BigDecimal(240);

  private final List<String> exclusiveList;

  public CarolinesYmlWrapper(YmlCatalog catalog, List<String> exclusiveList) {
    super(catalog);
    this.exclusiveList = exclusiveList;
  }

  @Override
  public CpaPolicy getCpaPolicy(Offer catalogOffer) {
    return CpaPolicy.FIXED;
  }

  @Override
  public BigDecimal getCost(Offer catalogOffer)
      throws NoInfoException {
    if (isExclusive(catalogOffer)) {
      return EXCLUSIVE_COST;
    }
    return REGULAR_COST;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer)
      throws NoInfoException {
    return exclusiveList.contains(catalogOffer.getId());
  }

  @Override
  public String getOfferTitle(Offer offer) {
    return YmlUtil.titleFor(offer, Vendor.class, Model.class);
  }
}
