package com.heymoose.domain.action;

public final class FixPriceActionData {

  private String token;
  private String transactionId;
  private ActionStatus status;
  private String offerCode;

  public String token() {
    return token;
  }

  public FixPriceActionData setToken(String token) {
    this.token = token;
    return this;
  }

  public String transactionId() {
    return transactionId;
  }

  public FixPriceActionData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public ActionStatus status() {
    return status;
  }

  public FixPriceActionData setStatus(ActionStatus status) {
    this.status = status;
    return this;
  }

  public String offerCode() {
    return offerCode;
  }

  public FixPriceActionData setOfferCode(String offerCode) {
    this.offerCode = offerCode;
    return this;
  }
}
