package com.heymoose.domain.action;

public abstract class ActionData {

  private String token;
  private String transactionId;
  private ActionStatus status;

  public String token() {
    return token;
  }

  public ActionData setToken(String token) {
    this.token = token;
    return this;
  }

  public String transactionId() {
    return transactionId;
  }

  public ActionData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public ActionStatus status() {
    return status;
  }

  public ActionData setStatus(ActionStatus status) {
    this.status = status;
    return this;
  }
}
