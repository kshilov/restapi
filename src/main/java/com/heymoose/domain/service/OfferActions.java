package com.heymoose.domain.service;

import com.heymoose.domain.model.action.OfferAction;
import com.heymoose.domain.model.offer.Offer;
import com.heymoose.infrastructure.hibernate.Transactional;

import java.util.Set;

public interface OfferActions {
  Integer approveExpired(Offer offer);

  Integer cancelByTransactions(Offer offer, Set<String> transactionIds);

  void approve(OfferAction action);

  void cancel(OfferAction action);

  @Transactional
  void fix();
}
