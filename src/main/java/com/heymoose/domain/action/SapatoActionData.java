package com.heymoose.domain.action;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public final class SapatoActionData extends ActionData {

  private String offerCode;
  private ActionStatus status;
  private BigDecimal price;

  public String offerCode() {
    return offerCode;
  }

  public SapatoActionData setOfferCode(String offerCode) {
    this.offerCode = offerCode;
    return this;
  }

  public ActionStatus status() {
    return status;
  }

  public SapatoActionData setStatus(ActionStatus status) {
    this.status = status;
    return this;
  }

  public BigDecimal price() {
    return this.price;
  }

  public SapatoActionData setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("transactionId", transactionId())
        .add("token", token())
        .add("status", status())
        .add("offerCode", offerCode())
        .add("price", price)
        .toString();
  }
}
