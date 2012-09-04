package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.offer.CpaPolicy;

import java.math.BigDecimal;

public interface YmlCatalogWrapper {

  public static class NoInfoException extends Exception {

    public NoInfoException(String msg) {
      super(msg);
    }
  }

  Iterable<Offer> listOffers();

  /**
   * Should return cpa policy for specific item. Can not return null.
   * @param catalogOffer item description
   * @param catalog whole catalog
   * @return cpa policy for item
   */
  CpaPolicy getCpaPolicy(Offer catalogOffer);
  String getOfferCode(Offer catalogOffer);
  String getOfferTitle(Offer catalogOffer);
  BigDecimal getPercent(Offer catalogOffer) throws NoInfoException;
  BigDecimal getCost(Offer catalogOffer) throws NoInfoException;
  boolean isExclusive(Offer catalogOffer) throws NoInfoException;

}
