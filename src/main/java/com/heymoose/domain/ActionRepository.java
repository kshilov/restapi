package com.heymoose.domain;

import java.util.Map;
import java.util.List;
import org.joda.time.DateTime;

import com.heymoose.domain.base.Repository;

public interface ActionRepository extends Repository<Action> {
  Action byPerformerAndOfferAndApp(long performerId, long offerId, long appId);
  Iterable<Action> list(Ordering ordering, int offset, int limit);
  Iterable<Action> list(Ordering ordering, int offset, int limit,
      Long offerId, Long appId, Long performerId);
  Iterable<Action> list(DateTime from, DateTime to,
      Long offerId, Long appId, Long performerId);

  Map<DateTime, Integer> stats(DateTime from, DateTime to,
                               Long offerId, Long appId, Long performerId, String trunc);
  Map<Long, Integer> countByApps(List<Long> appIds, DateTime from, DateTime to);
  Map<Long, Integer> countByOffers(List<Long> offerIds, DateTime from, DateTime to);

  
  long count();
  long count(Long offerId, Long appId, Long performerId);

  public enum Ordering {
    BY_CREATION_TIME_ASC,
    BY_CREATION_TIME_DESC
  }
}
