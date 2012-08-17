package com.heymoose.infrastructure.service.action;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class ActionDataImporterBase<T extends ActionData>
    implements ActionDataImporter<T>{

  private static final Logger log = LoggerFactory.getLogger(
      ActionDataImporterBase.class);

  protected final Repo repo;
  protected final OfferActions actions;
  protected final Tracking tracking;

  public ActionDataImporterBase(Repo repo,
                                Tracking tracking,
                                OfferActions actions) {
    this.repo = repo;
    this.actions = actions;
    this.tracking = tracking;
  }

  @Transactional
  @Override
  public void doImport(List<T> actionList, Long parentOfferId) {
    for (T action : actionList) {
      doImport(action, parentOfferId);
    }
  }

  @Override
  public final void doImport(T payment, Long parentOfferId) {
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", payment.token());
    if (token == null) {
      log.warn("Token '{}' not found, skipping", payment.token());
      return;
    }

    // check whether token was not tracked already
    List<OfferAction> offerActionList = repo.allByHQL(OfferAction.class,
        "from OfferAction where token = ? and transaction_id = ?",
        token, payment.transactionId());
    for (OfferAction offerAction : offerActionList) {
      if (offerAction.state().equals(OfferActionState.NOT_APPROVED) &&
          offerAction.transactionId().equals(payment.transactionId()) &&
          payment.status().equals(ActionStatus.CANCELED)) {
        log.info("Canceling action {}.", offerAction.id());
        actions.cancel(offerAction);
      }
    }
    if (offerActionList.size() > 0) {
      log.info("Transaction '{}' was already converted, offerActions='{}'. " +
          "Skipping..", payment.transactionId(), idList(offerActionList));
      return;
    }

    List<OfferAction> trackedActions = tracking.trackConversion(
        token, payment.transactionId(), extractOffers(payment, parentOfferId));

    if (payment.status().equals(ActionStatus.CANCELED)) {
      for (OfferAction action : trackedActions) {
        log.info("Cancelling just imported action '{}'.", action.id());
        actions.cancel(action);
      }
    }
  }

  /**
   * Process action data and return (offer - price) pairs for tracking.
   */
  protected abstract Multimap<BaseOffer, Optional<Double>> extractOffers(
      T actionData, Long parentOfferId);

  private List<Long> idList(Iterable<? extends IdEntity> entityList) {
    ImmutableList.Builder<Long> idList = ImmutableList.builder();
    for (IdEntity entity : entityList) {
      idList.add(entity.id());
    }
    return idList.build();
  }
}
