package com.heymoose.infrastructure.service.processing;

import com.heymoose.domain.request.Request;

public interface Processor {

  void process(Request request);
}
