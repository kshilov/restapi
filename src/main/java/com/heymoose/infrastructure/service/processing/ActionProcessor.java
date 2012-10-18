package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.infrastructure.service.processing.internal.IncLeadsProcessor;
import com.heymoose.infrastructure.service.processing.internal.IncSalesProcessor;
import com.heymoose.infrastructure.service.processing.internal.MoneyDivider;
import com.heymoose.infrastructure.service.processing.internal.MoneyDividers;
import com.heymoose.infrastructure.service.processing.internal.OfferStatProcessor;

import static com.heymoose.infrastructure.service.processing.ProcessorUtils.checkIfActionExists;

public final class ActionProcessor implements Processor {
  private static final OfferStatProcessor INC_LEADS =
      new IncLeadsProcessor();
  private static final OfferStatProcessor INC_SALES =
      new IncSalesProcessor();

  private CustomActionProcessor processor;
  private Repo repo;

  @Inject
  public ActionProcessor(Repo repo, CustomActionProcessor processor) {
    this.processor = processor;
    this.repo = repo;
  }

  public OfferAction process(ProcessableData data) {
    Tariff tariff = data.offer().tariff();
    switch (tariff.cpaPolicy()) {
      case FIXED:
        processor.setMoneyDivider(MoneyDividers.fix(tariff));
        processor.setOfferStatProcessor(INC_LEADS);
        break;
      case PERCENT:
        processor.setMoneyDivider(MoneyDividers.percent(tariff, data.price()));
        processor.setOfferStatProcessor(INC_SALES);
        break;
      case DOUBLE_FIXED:
        boolean actionExisted = checkIfActionExists(repo, data) != null;
        MoneyDivider divider = MoneyDividers.doubleFix(tariff, actionExisted);
        processor.setMoneyDivider(divider);
        processor.setOfferStatProcessor(INC_LEADS);
        break;
      default:
        throw new IllegalArgumentException("Unknown cpa policy. " +
            tariff.cpaPolicy());
    }
    return processor.process(data);
  }

}
