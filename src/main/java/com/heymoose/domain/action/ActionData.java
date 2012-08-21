package com.heymoose.domain.action;

import org.joda.time.DateTime;

public abstract class ActionData {

  private String token;
  private String transactionId;
  private DateTime creationTime;
  private DateTime lastChangeTime;
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

  public DateTime creationTime() {
    return this.creationTime;
  }

  public ActionData setCreationTime(DateTime creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public DateTime lastChangeTime() {
    return this.lastChangeTime;
  }

  public ActionData setLastChangeTime(DateTime lastChangeTime) {
    this.lastChangeTime = lastChangeTime;
    return this;
  }
}
