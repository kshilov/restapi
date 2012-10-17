package com.heymoose.infrastructure.service.processing;

import com.google.inject.Inject;

public final class ActionProcessor implements Processor {

  private final FixActionProcessor fixProcessor;

  @Inject
  public ActionProcessor(FixActionProcessor fixProcessor,
                         PercentActionProcessor percentProcessor) {
    this.fixProcessor = fixProcessor;
    this.percentProcessor = percentProcessor;
  }

  private final PercentActionProcessor percentProcessor;

  public void process(ProcessableData data) {
    switch (data.offer().cpaPolicy()) {
      case FIXED:
        fixProcessor.process(data);
        return;
      case PERCENT:
        percentProcessor.process(data);
        return;
    }

  }
}
