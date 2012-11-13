package com.heymoose.domain.action;

import com.google.common.base.Objects;
import org.joda.time.DateTime;

public abstract class ActionData {

  private String token;
  private String transactionId;
  private DateTime creationTime;
  private DateTime lastChangeTime;

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

  @Override
  public String toString() {
    return Objects.toStringHelper(ActionData.class)
        .add("token", token)
        .add("transaction", transactionId)
        .toString();
  }
}
