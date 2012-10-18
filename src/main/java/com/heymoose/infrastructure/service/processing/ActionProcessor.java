package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.service.processing.internal.MoneyDivider;
import com.heymoose.infrastructure.service.processing.internal.MoneyDividers;

import static com.heymoose.infrastructure.service.processing.ProcessorUtils.checkIfActionExists;

public final class ActionProcessor implements Processor {
  private CustomActionProcessor processor;
  private Repo repo;

  @Inject
  public ActionProcessor(Repo repo, CustomActionProcessor processor) {
    this.processor = processor;
    this.repo = repo;
  }

  public OfferAction process(ProcessableData data) {
    BaseOffer offer = data.offer();

    if (offer instanceof Offer && ((Offer) offer).isProductOffer()) {
      MoneyDivider divider = MoneyDividers.product(
          data.product(), (Offer) offer, data.price());
      processor.setMoneyDivider(divider);
      return processor.process(data);
    }

    Tariff tariff = offer.tariff();
    switch (tariff.cpaPolicy()) {
      case FIXED:
        processor.setMoneyDivider(MoneyDividers.fix(tariff));
        break;
      case PERCENT:
        processor.setMoneyDivider(MoneyDividers.percent(tariff, data.price()));
        break;
      case DOUBLE_FIXED:
        boolean actionExisted = checkIfActionExists(repo, data) != null;
        MoneyDivider divider = MoneyDividers.doubleFix(tariff, actionExisted);
        processor.setMoneyDivider(divider);
        break;
      default:
        throw new IllegalArgumentException("Unknown cpa policy. " +
            tariff.cpaPolicy());
    }
    return processor.process(data);
  }

}
