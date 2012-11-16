package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.cashback.Cashback;
import com.heymoose.domain.cashback.Cashbacks;
import com.heymoose.domain.statistics.OfferStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CashbackProcessor implements Processor {

  private static final Logger log =
      LoggerFactory.getLogger(CashbackProcessor.class);

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

    if (data.offerAction() == null) {
      log.warn("Skipping cashback processing. No action in {}", data);
      return;
    }

    Cashback cashback = new Cashback()
        .setTargetId(cashbackTargetId)
        .setAction(data.offerAction())
        .setAffiliate(data.offerAction().affiliate())
        .setReferrer(cashbackReferer);
    cashbacks.add(cashback);
    log.info("Cashback added {}. For {}", cashback, data);
  }
}
