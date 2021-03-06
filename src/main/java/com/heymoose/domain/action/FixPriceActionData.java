package com.heymoose.domain.action;

import com.google.common.base.Objects;

public final class FixPriceActionData extends ActionData {

  private String offerCode;
  private ActionStatus status;

  public String offerCode() {
    return offerCode;
  }

  public FixPriceActionData setOfferCode(String offerCode) {
    this.offerCode = offerCode;
    return this;
  }

  public ActionStatus status() {
    return status;
  }

  public FixPriceActionData setStatus(ActionStatus status) {
    this.status = status;
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
