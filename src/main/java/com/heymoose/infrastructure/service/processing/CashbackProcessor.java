package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;

public class CashbackProcessor implements Processor {

  private final Cashbacks cashbacks;

  @Inject
  public CashbackProcessor(Cashbacks cashbacks) {
    this.cashbacks = cashbacks;
  }

  @Override
  public void process(ProcessableData data) {
    String cashbackTargetId = data.token().stat().cashbackTargetId();
    if (cashbackTargetId == null) return;
    cashbacks.add(new Cashback()
        .setTargetId(cashbackTargetId)
        .setAction(data.offerAction())
        .setAffiliate(data.offerAction().affiliate()));
  }
}
