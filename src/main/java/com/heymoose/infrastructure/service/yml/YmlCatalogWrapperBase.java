package com.heymoose.infrastructure.service.yml;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.Map;

public abstract class YmlCatalogWrapperBase implements YmlCatalogWrapper {

  protected YmlCatalog catalog;

  public YmlCatalogWrapperBase(YmlCatalog catalog) {
    this.catalog = catalog;
  }

  public YmlCatalogWrapperBase wrapCatalog(YmlCatalog catalog) {
    this.catalog = catalog;
    return this;
  }

  @Override
  public Iterable<Offer> listOffers() {
    return catalog.getShop().getOffers().getOffer();
  }

  @Override
  public String getOfferCode(Offer catalogOffer) {
    return catalogOffer.getId();
  }

  @Override
  public String getOfferTitle(Offer offer) {
    Map<Class<?>, String> fields = YmlUtil.extractOptionalFields(offer,
        Name.class, Model.class);
    String name = fields.get(Name.class);
    String model = fields.get(Model.class);
    if (!Strings.isNullOrEmpty(name))
      return name;
    if (!Strings.isNullOrEmpty(model))
      return model;
    return offer.getDescription();
  }

  @Override
  public BigDecimal getPercent(Offer catalogOffer)
      throws NoInfoException {
    return null;
  }

  @Override
  public BigDecimal getCost(Offer catalogOffer)
      throws NoInfoException {
    return null;
  }

  @Override
  public boolean isExclusive(Offer catalogOffer)
      throws NoInfoException {
    return false;
  }
}
