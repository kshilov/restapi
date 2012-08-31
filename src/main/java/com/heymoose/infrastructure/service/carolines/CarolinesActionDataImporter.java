package com.heymoose.infrastructure.service.carolines;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class CarolinesActionDataImporter
    implements ActionDataImporter<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(CarolinesActionDataImporter.class);

  private static final Predicate<OfferAction> NOT_APPROVED =
      new Predicate<OfferAction>() {
      @Override
      public boolean apply(OfferAction offerAction) {
        return offerAction.state() == OfferActionState.NOT_APPROVED;
      }
    };

  private static Iterable<OfferAction> notApproved(Iterable<OfferAction> list) {
    return Iterables.filter(list, NOT_APPROVED);
  }

  private final Repo repo;
  private final OfferActions actionService;
  private final Tracking tracking;

  @Inject
  public CarolinesActionDataImporter(Repo repo,
                                     OfferActions actionService,
                                     Tracking tracking) {
    this.repo = repo;
    this.actionService = actionService;
    this.tracking = tracking;
  }

  @Transactional
  @Override
  public void doImport(List<ItemListActionData> actionList,
                       Long parentOfferId) {
    for (ItemListActionData data : actionList) {
      doImport(data, parentOfferId);
    }
  }

  @Override
  public void doImport(ItemListActionData payment, Long parentOfferId) {
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

    if (payment.status() == ActionStatus.CANCELED) {
      for (OfferAction action : notApproved(offerActionList)) {
        actionService.cancel(action);
      }
    }

    if (payment.status() == ActionStatus.COMPLETE) {
      for (OfferAction action : notApproved(offerActionList)) {
        actionService.approve(action);
      }
      // cancel, what's left unapproved
      for (OfferAction action : notApproved(offerActionList)) {
        actionService.cancel(action);
      }
    }


    if (offerActionList.size() > 0) {
      log.info("Transaction '{}' was already converted. " +
          "Skipping..", payment.transactionId());
      return;
    }

    List<OfferAction> trackedActions = tracking.trackConversion(
        token, payment.transactionId(), extractOffers(payment, parentOfferId));

    if (payment.status().equals(ActionStatus.CANCELED)) {
      for (OfferAction action : trackedActions) {
        log.info("Cancelling just imported action '{}'.", action.id());
        actionService.cancel(action);
      }
    }

    if (payment.status() == ActionStatus.COMPLETE) {
      for (OfferAction action : trackedActions) {
        log.info("Confirming just imported actions.");
        actionService.approve(action);
      }
    }
  }

  private Multimap<BaseOffer, Optional<Double>> extractOffers(
      ItemListActionData payment, Long parentOfferId) {

    ImmutableMultimap.Builder<BaseOffer, Optional<Double>> offerMap =
        ImmutableMultimap.builder();
    for (ItemListActionData.Item item : payment.itemList()) {
      BaseOffer productOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOfferId, item.id());
      if (productOffer == null) {
        log.warn("Product with code '{}' does not present in our db! " +
            "Parent offer: '{}'. Skipping..", item.id(), parentOfferId);
        continue;
      }
      BigDecimal price = item.price();
      for (int i = 1; i <= item.quantity(); i++) {
        log.info("Adding conversion for offer '{}' code '{}' price '{}'",
            new Object[] {
                productOffer.id() ,
                productOffer.code(),
                price });
        offerMap.put(productOffer, Optional.of(price.doubleValue()));
      }
    }
    return offerMap.build();
  }
}
