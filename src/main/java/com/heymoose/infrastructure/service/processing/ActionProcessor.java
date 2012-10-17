package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;

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

  public void process(ProcessableData data) {
    switch (data.offer().cpaPolicy()) {
      case FIXED:
        fixProcessor.process(data);
        return;
      case PERCENT:
        percentProcessor.process(data);
        return;
      case DOUBLE_FIXED:
        doubleFixProcessor.process(data);
        return;
    }

  }
}
