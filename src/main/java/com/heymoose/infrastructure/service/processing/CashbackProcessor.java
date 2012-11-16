package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.domain.statistics.OfferStat;

public class CashbackProcessor implements Processor {

  private final Cashbacks cashbacks;

  @Inject
  public CashbackProcessor(Cashbacks cashbacks) {
    this.cashbacks = cashbacks;
  }

  @Override
  public void process(ProcessableData data) {
    if (!data.offer().masterOffer().allowCashback()) return;

    OfferStat source = data.token().stat();
    String cashbackTargetId = source.cashbackTargetId();
    if (cashbackTargetId == null) return;
    String cashbackReferer = source.cashbackReferrer();

    cashbacks.add(new Cashback()
        .setTargetId(cashbackTargetId)
        .setAction(data.offerAction())
        .setAffiliate(data.offerAction().affiliate())
        .setReferrer(cashbackReferer));
  }
}
