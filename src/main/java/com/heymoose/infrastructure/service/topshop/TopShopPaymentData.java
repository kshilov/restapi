package com.heymoose.infrastructure.service.topshop;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.util.Map;

public final class TopShopPaymentData {

  private String heymooseToken;
  private String transactionId;
  private final Map<String, BigDecimal> itemPriceMap = Maps.newHashMap();

  public TopShopPaymentData setToken(String token) {
    this.heymooseToken = token;
    return this;
  }

  public TopShopPaymentData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public TopShopPaymentData addItem(String code, BigDecimal price) {
    this.itemPriceMap.put(code, price);
    return this;
  }

  public TopShopPaymentData addItem(String code, String priceAsString) {
    this.itemPriceMap.put(code, new BigDecimal(priceAsString));
    return this;
  }

  public String token() {
    return this.heymooseToken;
  }

  public String transactionId() {
    return this.transactionId;
  }

  public BigDecimal price(String item) {
    return itemPriceMap.get(item);
  }

  public ImmutableMap<String, BigDecimal> itemPriceMap() {
    return ImmutableMap.copyOf(itemPriceMap);
  }

}
