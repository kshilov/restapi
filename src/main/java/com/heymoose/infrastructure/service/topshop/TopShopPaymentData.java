package com.heymoose.infrastructure.service.topshop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

public final class TopShopPaymentData {

  private String heymooseToken;
  private String transactionId;
  private final List<String> itemList = Lists.newArrayList();

  public TopShopPaymentData setToken(String token) {
    this.heymooseToken = token;
    return this;
  }

  public TopShopPaymentData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public TopShopPaymentData addItem(String code) {
    this.itemList.add(code);
    return this;
  }

  public String token() {
    return this.heymooseToken;
  }

  public String transactionId() {
    return this.transactionId;
  }

  public List<String> items() {
    return ImmutableList.copyOf(itemList);
  }

}
