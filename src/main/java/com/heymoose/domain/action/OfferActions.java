package com.heymoose.domain.action;

import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OfferActions {

  enum Ordering {
    TRANSACTION_ID, AFFILIATE_ID, AFFILIATE_EMAIL, CREATION_TIME,
    LAST_CHANGE_TIME,  STATE,
    AMOUNT, OFFER_CODE, OFFER_TITLE
  }

  enum DateKind { CREATION, CHANGE }

  Integer approveExpired(Offer offer);

  Integer cancelByTransactions(Offer offer, Set<String> transactionIds);

  void approve(OfferAction action);

  void cancel(OfferAction action);

  void cancelByIdList(Offer offer, Collection<Long> idCollection);

  void approveByIdList(Offer offer, List<Long> idList);

  int verify(Offer offer, Collection<String> transactionIdList,
             OfferActionState state);



  @Transactional
  void fix();

  Pair<QueryResult, Long> list(Long offerId, OfferActionState state,
                               DateKind dateKind,
                               DataFilter<Ordering> filter);

  List<OfferAction> listProductActions(Token token,
                                       String transactionId,
                                       Product product);

  List<OfferAction> list(Token token, String transactionId);

}
