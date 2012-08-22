package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

@Singleton
public final class OfferActionsStoredFunc implements OfferActions {

  private static final Logger log =
      LoggerFactory.getLogger(OfferActionsStoredFunc.class);

  private final OfferActionsHiber offerActionsHiber;
  private final Repo repo;

  @Inject
  public OfferActionsStoredFunc(OfferActionsHiber offerActionsHiber, Repo repo) {
    this.offerActionsHiber = offerActionsHiber;
    this.repo = repo;
  }
  @Override
  public Integer approveExpired(Offer offer) {
    DateTime start = DateTime.now();

    if (offer != null) {
      return repo.session()
          .createSQLQuery("select approve_expired(:offer_id)")
          .setParameter("offer_id", offer.id())
          .list().size();
    }
    int size = repo.session()
        .createSQLQuery("select approve_expired();")
        .list().size();

    log.info("Total approve time: {}",
        Period.fieldDifference(
            DateTime.now().toLocalTime(),
            start.toLocalTime()));
    return size;
  }

  @Override
  public Integer cancelByTransactions(Offer offer, Set<String> transactionIds) {
    return offerActionsHiber.cancelByTransactions(offer, transactionIds);
  }

  @Override
  public void approve(OfferAction action) {
    offerActionsHiber.approve(action);
  }

  @Override
  public void cancel(OfferAction action) {
    offerActionsHiber.cancel(action);
  }

  @Override
  public void cancelByIdList(Offer offer, Collection<Long> idCollection) {
    offerActionsHiber.cancelByIdList(offer, idCollection);
  }

  @Override
  public void fix() {
    offerActionsHiber.fix();
  }

  @Override
  public Pair<QueryResult, Long> list(Long offerId, OfferActionState state,
                                      ListFilter filter,
                                      Ordering ordering,
                                      OrderingDirection direction) {
    return offerActionsHiber.list(offerId, state, filter, ordering, direction);
  }
}
