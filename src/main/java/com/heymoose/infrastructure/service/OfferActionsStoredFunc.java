package com.heymoose.infrastructure.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.db.QueryResult;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
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
  public void approveByIdList(Offer offer, List<Long> idList) {
    offerActionsHiber.approveByIdList(offer, idList);
  }

  @Override
  public int verify(Offer offer, Collection<String> transactionIdList,
                    OfferActionState state) {
    return offerActionsHiber.verify(offer, transactionIdList, state);
  }

  @Override
  public void fix() {
    offerActionsHiber.fix();
  }

  @Override
  public Pair<QueryResult, Long> list(Long offerId, OfferActionState state,
                                      DateKind dateKind,
                                      DataFilter<Ordering> filter) {
    return offerActionsHiber.list(offerId, state, dateKind, filter);
  }
}
