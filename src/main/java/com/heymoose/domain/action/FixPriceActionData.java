package com.heymoose.domain.action;

public final class FixPriceActionData extends ActionData {

  private String offerCode;

  public String offerCode() {
    return offerCode;
  }

  public FixPriceActionData setOfferCode(String offerCode) {
    this.offerCode = offerCode;
    return this;
  }
}
