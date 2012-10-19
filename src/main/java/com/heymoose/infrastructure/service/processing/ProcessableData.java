package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;

import java.math.BigDecimal;

public final class ProcessableData {

  private boolean processed;
  private BigDecimal price;
  private BaseOffer offer;
  private Token token;
  private String transactionId;
  private Product product;

  public ProcessableData setProcessed(boolean processed) {
    this.processed = processed;
    return this;
  }

  public boolean isProcessed() {
    return this.processed;
  }

  public BaseOffer offer() {
    return offer;
  }

  public ProcessableData setOffer(BaseOffer offer) {
    this.offer = offer;
    return this;
  }

  public ProcessableData setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  public BigDecimal price() {
    return price;
  }

  public Product product() {
    return this.product;
  }

  public ProcessableData setProduct(Product product) {
    this.product = product;
    return this;
  }

  public ProcessableData setTransactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public ProcessableData setToken(Token token) {
    this.token = token;
    return this;
  }

  public Token token() {
    return this.token;
  }

  public String transactionId() {
    return this.transactionId;
  }


}
