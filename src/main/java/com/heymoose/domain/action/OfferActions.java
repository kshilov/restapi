package com.heymoose.domain.action;

import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.persistence.Transactional;

import java.util.Set;

public interface OfferActions {
  Integer approveExpired(Offer offer);

  Integer cancelByTransactions(Offer offer, Set<String> transactionIds);

  void approve(OfferAction action);

  void cancel(OfferAction action);

  @Transactional
  void fix();
}
