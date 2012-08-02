package com.heymoose.infrastructure.service.topshop;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Named;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class TopShopDataImporter {

  private static final Logger log =
      LoggerFactory.getLogger(TopShopDataImporter.class);

  private final Repo repo;
  private final Tracking tracking;
  private final OfferActions actions;
  private final Long parentOfferId;

  @Inject
  public TopShopDataImporter(@Named("topshop.offer") String parentOffer,
                             Repo repo, Tracking tracking,
                             OfferActions actions) {
    this.repo = repo;
    this.tracking = tracking;
    this.actions = actions;
    this.parentOfferId = Long.valueOf(parentOffer);
  }

  @Transactional
  public void doImport(List<ActionData> paymentList) {
    for (ActionData payment : paymentList) {
      doImport(payment);
    }
  }

  private void doImport(ActionData payment) {
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", payment.token());
    if (token == null) {
      log.warn("Token {} not found, skipping", payment.token());
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
      log.warn("Token {} was already converted, offerAction={}, skipping",
          payment.token(), offerAction.id());
      return;
    }

    ImmutableMap.Builder<BaseOffer, Optional<Double>> offerMap =
        ImmutableMap.builder();
    for (ActionData.Item item : payment.itemList()) {
      BaseOffer topshopOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOfferId, String.valueOf(item.id()));
      if (topshopOffer == null) {
        log.warn("Product with code '{}' does not present in our db! " +
            "Using default offer '{}'.", item.id(), parentOfferId);
        topshopOffer = repo.byHQL(Offer.class,
            "from Offer where id = ?", parentOfferId);
      }
      BigDecimal price = namePrice(item, topshopOffer);
      log.info("Adding conversion for offer '{}' code '{}' price '{}'",
          new Object[] {
              topshopOffer.id() ,
              topshopOffer.code(),
              price });
      offerMap.put(topshopOffer, Optional.of(price.doubleValue()));
    }
    List<OfferAction> trackedActions = tracking.trackConversion(
        token, payment.transactionId(), offerMap.build());

    if (payment.status().equals(ActionData.Status.CANCELED)) {
      for (OfferAction action : trackedActions) {
        log.info("Canceling just imported action {}.", action.id());
        actions.cancel(action);
      }
    }
  }

  private BigDecimal namePrice(ActionData.Item item, BaseOffer offer) {
    return offer.cost();
  }
}
