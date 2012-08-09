package com.heymoose.domain.action;

import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.ListFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;

import java.util.Set;

public interface OfferActions {
  enum Ordering {
    TRANSACTION_ID, AFFILIATE_ID, AFFILIATE_EMAIL, CREATION_TIME, STATE,
    AMOUNT
  }

  Integer approveExpired(Offer offer);

  Integer cancelByTransactions(Offer offer, Set<String> transactionIds);

  void approve(OfferAction action);

  void cancel(OfferAction action);

  @Transactional
  void fix();

  Pair<QueryResult, Long> list(Long offerId, OfferActionState state,
                               ListFilter filter,
                               Ordering ordering, OrderingDirection direction);

}
