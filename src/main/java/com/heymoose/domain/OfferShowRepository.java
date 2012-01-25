package com.heymoose.domain;

import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

import com.heymoose.domain.base.Repository;

public interface OfferShowRepository extends Repository<OfferShow> {
  Map<DateTime, Integer> stats(DateTime from, DateTime to,
                               Long offerId, Long appId, Long performerId, String trunc);
  Map<Long, Integer> countByApps(List<Long> appIds, DateTime from, DateTime to);
  Map<Long, Integer> countByOffers(List<Long> offerIds, DateTime from, DateTime to);

  Long count(Long offerId, Long appId, Long performerId);
}
