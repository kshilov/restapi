package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class ActionDataImporterBase<T extends ActionData>
    implements ActionDataImporter<T> {

  private static final Logger log = LoggerFactory.getLogger(
      ActionDataImporterBase.class);

  protected final Repo repo;
  protected final OfferActions actions;

  public ActionDataImporterBase(Repo repo,
                                OfferActions actions) {
    this.repo = repo;
    this.actions = actions;
  }

  @Transactional
  @Override
  public void doImport(List<T> actionList, Long parentOfferId) {
    log.info("Starting import for {} actions.", actionList.size());
    for (T action : actionList) {
      doImport(action, parentOfferId);
    }
  }

  @Override
  public final void doImport(T payment, Long parentOfferId) {
    log.info("Starting import for payment: {}", payment);
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", payment.token());
    if (token == null) {
      log.warn("Token '{}' not found, skipping", payment.token());
      return;
    }

    if (payment.transactionId() == null) {
      log.warn("Transaction id is null for payment: {}. Skipping..", payment);
      return;
    }

    // check whether token was not tracked already
    List<OfferAction> offerActionList = repo.allByHQL(OfferAction.class,
        "from OfferAction where token = ? and transaction_id = ?",
        token, payment.transactionId());
    for (OfferAction offerAction : offerActionList) {
      if (offerAction.state().equals(OfferActionState.NOT_APPROVED) &&
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
    Offer parentOffer = repo.get(Offer.class, parentOfferId);
    if (parentOffer == null) {
      log.warn("Unknown parent offer: {}", parentOfferId);
      throw new IllegalArgumentException("Unknown parent offer id " +
          parentOfferId);
    }

    List<OfferAction> trackedActions = process(payment, parentOffer);

    if (payment.status().equals(ActionStatus.CANCELED)) {
      for (OfferAction action : trackedActions) {
        log.info("Cancelling just imported action '{}'.", action.id());
        actions.cancel(action);
      }
    }
  }


  protected abstract List<OfferAction> process(T actionData, Offer parentOffer);

  private List<Long> idList(Iterable<? extends IdEntity> entityList) {
    ImmutableList.Builder<Long> idList = ImmutableList.builder();
    for (IdEntity entity : entityList) {
      idList.add(entity.id());
    }
    return idList.build();
  }
}
