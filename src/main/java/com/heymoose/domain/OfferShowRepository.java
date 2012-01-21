package com.heymoose.domain;

import java.util.Map;
import org.joda.time.DateTime;

import com.heymoose.domain.base.Repository;

public interface OfferShowRepository extends Repository<OfferShow> {
  Map<DateTime, Integer> stats(DateTime from, DateTime to,
                               Long offerId, Long appId, Long performerId, String trunc);

  Long count(Long offerId, Long appId, Long performerId);
}
