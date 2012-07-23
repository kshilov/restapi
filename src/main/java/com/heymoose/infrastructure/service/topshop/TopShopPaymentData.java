package com.heymoose.infrastructure.service.topshop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

public final class TopShopPaymentData {

  public enum Status {
    CREATED, COMPLETE, CANCELED
  }

  private String heymooseToken;
  private String transactionId;
  private Status status = Status.CREATED;
  private final List<Long> itemList = Lists.newArrayList();

  public TopShopPaymentData setToken(String token) {
    this.heymooseToken = token;
    return this;
  }

  public TopShopPaymentData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public TopShopPaymentData addItem(Long code) {
    this.itemList.add(code);
    return this;
  }

  public TopShopPaymentData setStatus(Status status) {
    this.status = status;
    return this;
  }

  public String token() {
    return this.heymooseToken;
  }

  public String transactionId() {
    return this.transactionId;
  }

  public List<Long> items() {
    return ImmutableList.copyOf(itemList);
  }

  public Status status() {
    return this.status;
  }

}
