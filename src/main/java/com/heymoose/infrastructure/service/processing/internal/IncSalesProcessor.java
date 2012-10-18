package com.heymoose.infrastructure.service.processing.internal;

import com.heymoose.domain.statistics.OfferStat;

public final class IncSalesProcessor implements OfferStatProcessor {
  @Override
  public OfferStat process(OfferStat stat) {
    return stat.incSales();
  }
}
