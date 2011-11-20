package com.heymoose.domain;

import com.heymoose.domain.base.Repository;

import java.util.Set;

public interface OfferRepository extends Repository<Offer> {
  Set<Offer> availableFor(Performer performer);
  Set<Offer> doneFor(long performerId);
}
