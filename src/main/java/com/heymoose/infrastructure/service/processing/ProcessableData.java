package com.heymoose.infrastructure.service.processing;

import com.google.common.base.Objects;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.statistics.Token;

import java.math.BigDecimal;

public final class ProcessableData {

  private BigDecimal price;
  private BaseOffer offer;
  private Token token;
  private String transactionId;
  private Product product;
  private OfferAction offerAction;
  private Site site;

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

  public ProcessableData setOfferAction(OfferAction action) {
    this.offerAction = action;
    return this;
  }

  public OfferAction offerAction() {
    return this.offerAction;
  }

  public ProcessableData setSite(Site site) {
    this.site = site;
    return this;
  }

  public Site site() {
    return this.site;
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(ProcessableData.class)
        .add("token", token)
        .add("transactionId", transactionId)
        .add("offer", offer)
        .add("product", product)
        .add("price", price)
        .toString();
  }

}
