package com.heymoose.domain.action;

import com.google.common.base.Objects;

public final class FixPriceActionData extends ActionData {

  private String offerCode;

  public String offerCode() {
    return offerCode;
  }

  public FixPriceActionData setOfferCode(String offerCode) {
    this.offerCode = offerCode;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("transactionId", transactionId())
        .add("token", token())
        .add("status", status())
        .add("offerCode", offerCode())
        .toString();
  }
}
