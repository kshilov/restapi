package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.action.OfferAction;

public interface Processor {

  OfferAction process(ProcessableData data);
}
