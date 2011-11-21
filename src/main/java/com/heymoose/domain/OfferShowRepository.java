package com.heymoose.domain;

import org.joda.time.DateTime;

import com.heymoose.domain.base.Repository;

public interface OfferShowRepository extends Repository<OfferShow> {
  Iterable<OfferShow> list(DateTime from, DateTime to,
      Long offerId, Long appId, Long performerId);
}
