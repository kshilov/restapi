package com.heymoose.domain.affiliate;

import com.heymoose.hibernate.Transactional;

import java.util.Set;

public interface OfferActions {
  Integer approveExpired(Offer offer);

  Integer cancelByTransactions(Offer offer, Set<String> transactionIds);

  void approve(OfferAction action);

  void cancel(OfferAction action);

  @Transactional
  void fix();
}
