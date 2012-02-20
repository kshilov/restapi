package com.heymoose.domain;

import com.heymoose.domain.base.Repository;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

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
  Map<Integer, Integer> audienceByYears(Long offerId, Long appId, DateTime from, DateTime to);
  Map<Boolean, Integer> audienceByGenders(Long offerId, Long appId, DateTime from, DateTime to);
  Map<String, Integer> audienceByCities(Long offerId, Long appId, DateTime from, DateTime to);

  
  long count();
  long count(Long offerId, Long appId, Long performerId);

  public enum Ordering {
    BY_CREATION_TIME_ASC,
    BY_CREATION_TIME_DESC
  }
}
