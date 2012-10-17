package com.heymoose.infrastructure.service.processing;

import com.google.common.collect.Maps;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.offer.BaseOffer;

import java.util.EnumMap;
import java.util.Map;

public final class ProcessableData extends ActionData implements Cloneable {

  protected boolean processed;
  protected EnumMap<ActionParam, String> paramMap =
      Maps.newEnumMap(ActionParam.class);
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

  public Map<ActionParam, String> paramMap() {
    return this.paramMap;
  }

  public String paramValue(ActionParam key) {
    return paramMap.get(key);
  }

  public ProcessableData putParam(ActionParam key, String value) {
    this.paramMap.put(key, value);
    return this;
  }

  @Override
  public ProcessableData clone() {
    ProcessableData data = new ProcessableData();
    data.setToken(this.token());
    data.setTransactionId(this.transactionId());
    data.paramMap = Maps.newEnumMap(this.paramMap);
    data.offer = this.offer;
    data.processed = processed;
    return data;
  }
}
