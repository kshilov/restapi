package com.heymoose.infrastructure.service.sapato;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.FixPriceActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.action.ActionDataImporter;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import com.heymoose.infrastructure.service.processing.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SapatoImporter
    implements ActionDataImporter<FixPriceActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(SapatoImporter.class);

  private final Repo repo;
  private final OfferActions actions;
  private final Processor processor;

  @Inject
  public SapatoImporter(Repo repo, OfferActions actions,
                        Processor processor) {
    this.repo = repo;
    this.actions = actions;
    this.processor = processor;
  }

  @Transactional
  @Override
  public void doImport(List<FixPriceActionData> actionList,
                       Long parentOfferId) {
    log.info("Starting import for {} action(s).", actionList.size());
    for (FixPriceActionData actionData : actionList) {
      doImport(actionData, parentOfferId);
    }
  }

  @Override
  public void doImport(FixPriceActionData actionData, Long parentOfferId) {
    log.info("Entering doImport for {}, parentOfferId: {}.",
        actionData, parentOfferId);
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", actionData.token());
    if (token == null) {
      log.warn("Token '{}' not found, skipping", actionData.token());
      return;
    }

    // check whether token was not tracked already
    List<OfferAction> offerActionList = repo.allByHQL(OfferAction.class,
        "from OfferAction where token = ? and transaction_id = ?",
        token, actionData.transactionId());
    for (OfferAction offerAction : offerActionList) {
      if (offerAction.state() != OfferActionState.NOT_APPROVED) {
        log.info("Imported action found: {}.", offerAction.id());
        continue;
      }

      if (actionData.status() == ActionStatus.CANCELED) {
        log.info("Canceling action {}.", offerAction.id());
        actions.cancel(offerAction);
      }

      if (actionData.status() == ActionStatus.COMPLETE &&
          offerAction.offer().code().equals(actionData.offerCode())) {
        BaseOffer offer = offerAction.offer();
        log.info("Confirming action '{}' for " +
            "offer id: '{}' code: '{}' title: '{}'", new Object[]{
            offerAction.id(), offer.id(), offer.code(), offer.title()});

        actions.approve(offerAction);
      }

      if (actionData.status() == ActionStatus.COMPLETE &&
          !offerAction.offer().code().equals(actionData.offerCode())) {
        BaseOffer prev = offerAction.offer();
        BaseOffer cur = findSubOffer(parentOfferId, actionData.offerCode());
        log.info("Offer changed for action: '{}', " +
            "previous offer: '{}' - '{}' " +
            "current: '{}' - '{}'", new Object[]{
            offerAction.id(), prev.id(), prev.title(), cur.id(), cur.title()});
        actions.cancel(offerAction);

        List<OfferAction> actionList = process(actionData, cur, token);

        for (OfferAction resultAction : actionList) {
          resultAction.setCreationTime(offerAction.creationTime());
          repo.put(resultAction);
          actions.approve(resultAction);
        }
      }
    }

    if (offerActionList.size() > 0) {
      log.info("Transaction '{}' was processed.",
          actionData.transactionId());
      return;
    } else {
      log.info("Transaction '{}' was not yet processed. Processing..",
          actionData.transactionId());
    }

    if (Strings.isNullOrEmpty(actionData.offerCode())) {
      log.info("OfferCode is empty, skipping..");
      return;
    }

    BaseOffer subOffer = findSubOffer(parentOfferId, actionData.offerCode());

    if (subOffer == null) {
      log.info("Offer with code '{}' not found for parent: '{}'",
          actionData.offerCode(), parentOfferId);
      return;
    }

    List<OfferAction> resultActions = process(actionData, subOffer, token);

    if (actionData.status() == ActionStatus.CANCELED) {
      log.info("Cancelling just imported actions.");
      for (OfferAction actionToCancel : resultActions) {
        actions.cancel(actionToCancel);
      }
    }

    if (actionData.status() == ActionStatus.COMPLETE) {
      log.info("Confirming just imported actions.");
      for (OfferAction actionToApprove : resultActions) {
        actions.approve(actionToApprove);
      }
    }

  }

  private List<OfferAction> process(FixPriceActionData actionData,
                                    BaseOffer offer, Token token) {
    ProcessableData data = new ProcessableData()
        .setToken(token)
        .setTransactionId(actionData.transactionId())
        .setOffer(offer);
    this.processor.process(data);
    return ImmutableList.of(data.offerAction());
  }

  private BaseOffer findSubOffer(Long parentId, String code) {
    SubOffer sub = repo.byHQL(SubOffer.class,
            "from SubOffer where parent_id = ? and code = ?",
            parentId, code);
    if (sub != null) {
      return sub;
    }
    return repo.byHQL(Offer.class,
        "from Offer where id = ? and code = ?", parentId, code);
  }
}
