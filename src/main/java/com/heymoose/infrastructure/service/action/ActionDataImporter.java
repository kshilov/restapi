package com.heymoose.infrastructure.service.action;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public abstract class ActionDataImporter {
  private static final Logger log =
      LoggerFactory.getLogger(ActionDataImporter.class);

  private final Repo repo;
  private final Tracking tracking;
  private final OfferActions actions;

  public ActionDataImporter(Repo repo, Tracking tracking,
                            OfferActions actions) {
    this.repo = repo;
    this.tracking = tracking;
    this.actions = actions;
  }

  @Transactional
  public void doImport(List<ActionData> paymentList, Long parentOfferId) {
    for (ActionData payment : paymentList) {
      doImport(payment, parentOfferId);
    }
  }

  private void doImport(ActionData payment, Long parentOfferId) {
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", payment.token());
    if (token == null) {
      log.warn("Token '{}' not found, skipping", payment.token());
      return;
    }

    // check whether token was not tracked already
    OfferAction offerAction = repo.byHQL(OfferAction.class,
        "from OfferAction where token = ?", token);
    if (offerAction != null) {
      if (offerAction.state().equals(OfferActionState.NOT_APPROVED) &&
          payment.status().equals(ActionData.Status.CANCELED)) {
        log.info("Canceling action {}.", offerAction.id());
        actions.cancel(offerAction);
        return;
      }
      log.warn("Token '{}' was already converted, offerAction='{}', skipping",
          payment.token(), offerAction.id());
      return;
    }

    ImmutableMap.Builder<BaseOffer, Optional<Double>> offerMap =
        ImmutableMap.builder();
    for (ActionData.Item item : payment.itemList()) {
      BaseOffer productOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOfferId, item.id());
      if (productOffer == null) {
        log.warn("Product with code '{}' does not present in our db! " +
            "Parent offer: '{}'. Skipping..", item.id(), parentOfferId);
        continue;
      }
      BigDecimal price = namePrice(item, productOffer);
      log.info("Adding conversion for offer '{}' code '{}' price '{}'",
          new Object[] {
              productOffer.id() ,
              productOffer.code(),
              price });
      offerMap.put(productOffer, Optional.of(price.doubleValue()));
    }
    List<OfferAction> trackedActions = tracking.trackConversion(
        token, payment.transactionId(), offerMap.build());

    if (payment.status().equals(ActionData.Status.CANCELED)) {
      for (OfferAction action : trackedActions) {
        log.info("Canceling just imported action '{}'.", action.id());
        actions.cancel(action);
      }
    }
  }

  protected abstract BigDecimal namePrice(ActionData.Item item, BaseOffer offer);
}
