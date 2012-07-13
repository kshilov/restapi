package com.heymoose.domain.affiliate.repository;

import com.heymoose.domain.affiliate.Offer;
import com.heymoose.domain.affiliate.OfferAction;
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
