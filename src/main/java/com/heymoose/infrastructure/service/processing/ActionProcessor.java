package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;
import com.heymoose.domain.action.OfferAction;

public final class ActionProcessor implements Processor {

  private final FixActionProcessor fixProcessor;
  private final PercentActionProcessor percentProcessor;
  private final DoubleFixActionProcessor doubleFixProcessor;

  @Inject
  public ActionProcessor(FixActionProcessor fixProcessor,
                         PercentActionProcessor percentProcessor,
                         DoubleFixActionProcessor doubleFixProcessor) {
    this.fixProcessor = fixProcessor;
    this.percentProcessor = percentProcessor;
    this.doubleFixProcessor = doubleFixProcessor;
  }

  public OfferAction process(ProcessableData data) {
    switch (data.offer().cpaPolicy()) {
      case FIXED:
        return fixProcessor.process(data);
      case PERCENT:
        return percentProcessor.process(data);
      case DOUBLE_FIXED:
        return doubleFixProcessor.process(data);
    }
    throw new IllegalArgumentException("Unknown cpa policy.");
  }
}
