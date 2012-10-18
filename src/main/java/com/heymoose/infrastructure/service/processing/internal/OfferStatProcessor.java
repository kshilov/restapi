package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.statistics.OfferStat;

public interface OfferStatProcessor {

  OfferStat process(OfferStat stat);

}
