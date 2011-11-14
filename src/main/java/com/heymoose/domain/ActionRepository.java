package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

public interface ActionRepository extends Repository<Action> {
  Action byPerformerAndOffer(long performerId, long offerId);
  Iterable<Action> list(Ordering ordering, int offset, int limit);
  long count();

  public enum Ordering {
    BY_CREATION_TIME_ASC,
    BY_CREATION_TIME_DESC
  }
}
