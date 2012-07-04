package com.heymoose.domain.affiliate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.affiliate.base.Repo;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
          .setParameter("offer_id", offer.id)
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
    DateTime start = DateTime.now();
    repo.session().createSQLQuery("select approve(:action_id)")
        .setParameter("action_id", action.id())
        .uniqueResult();
    log.info("Approve time: {}",
        Period.fieldDifference(
            DateTime.now().toLocalTime(),
            start.toLocalTime()));
  }

  @Override
  public void cancel(OfferAction action) {
    offerActionsHiber.cancel(action);
  }

  @Override
  public void fix() {
    offerActionsHiber.fix();
  }
}
