package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.offer.BaseOffer;

import java.math.BigDecimal;

public final class ProcessableData extends ActionData implements Cloneable {


  public static ProcessableData copyActionData(ActionData actionData) {
    ProcessableData data = new ProcessableData();
    data.setTransactionId(actionData.transactionId())
        .setToken(actionData.token());
    return data;
  }

  protected boolean processed;
  protected BigDecimal price;
  protected BaseOffer offer;

  public ProcessableData setProcessed(boolean processed) {
    this.processed = processed;
    return this;
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

  @Override
  public ProcessableData clone() {
    ProcessableData data = new ProcessableData();
    data.setToken(this.token());
    data.setTransactionId(this.transactionId());
    data.price = this.price;
    data.offer = this.offer;
    data.processed = processed;
    return data;
  }

}
