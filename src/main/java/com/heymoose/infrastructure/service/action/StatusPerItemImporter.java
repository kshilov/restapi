package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.ItemWithStatus;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.action.StatusPerItemActionData;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.OfferLoader;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.service.Tokens;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import com.heymoose.infrastructure.service.processing.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class StatusPerItemImporter
    implements ActionDataImporter<StatusPerItemActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(StatusPerItemImporter.class);

  private final Products products;
  private final Tokens tokens;
  private final OfferActions actions;
  private final Processor processor;
  private final OfferLoader offers;

  public StatusPerItemImporter(Tokens tokens,
                               OfferLoader offers,
                               Products products,
                               OfferActions actions,
                               Processor processor) {
    this.processor = processor;
    this.offers = offers;
    this.products = products;
    this.tokens = tokens;
    this.actions = actions;
  }

  @Override
  public void doImport(List<StatusPerItemActionData> actionList,
                       Long parentOfferId) {
    for (StatusPerItemActionData action : actionList) {
      doImport(action, parentOfferId);
    }
  }

  @Override
  public void doImport(StatusPerItemActionData actionData, Long parentOfferId) {

    Token token = tokens.byValue(actionData.token());
    Offer offer = offers.offerById(parentOfferId);

    if (token == null) {
      log.info("Token not found. Skipping {}", actionData);
      return;
    }

    if (offer == null) {
      log.info("Offer not found. Skipping {}", actionData);
      return;
    }

    List<OfferAction> actionList = actions.list(
        token, actionData.transactionId());
    actionList = Lists.newArrayList(actionList);
    if (actionList.isEmpty()) {
      for (ItemWithStatus item: actionData.itemList()) {
          Product product = products.byOriginalId(parentOfferId, item.id());
          ProcessableData data = new ProcessableData()
              .setToken(token)
              .setOffer(offer)
              .setProduct(product)
              .setPrice(item.price())
              .setTransactionId(actionData.transactionId());
          processor.process(data);
          actionList.add(data.offerAction());
      }
    }
    List<ItemWithStatus> notMatchedItems =
        Lists.newArrayList(actionData.itemList());

    removeMatchedItems(notMatchedItems, actionList,
        ActionStatus.COMPLETE, OfferActionState.APPROVED);

    removeMatchedItems(notMatchedItems, actionList,
        ActionStatus.CANCELED, OfferActionState.CANCELED);

    for (OfferAction action : notApproved(actionList)) {
      Iterator<ItemWithStatus> itemIterator = notMatchedItems.iterator();
      while (itemIterator.hasNext()) {
        ItemWithStatus item = itemIterator.next();
        if (action.product().originalId().equals(item.id())) {
          if (item.status() == ActionStatus.COMPLETE) {
            log.info("Approving action {}. {}", action, item);
            actions.approve(action);
            itemIterator.remove();
          }
          if (item.status() == ActionStatus.CANCELED) {
            log.info("Cancelling action {}. {}", action, item);
            actions.cancel(action);
            itemIterator.remove();
          }
        }
      }
    }
  }

  private void removeMatchedItems(List<ItemWithStatus> notMatchedItems,
                                  List<OfferAction> actionList,
                                  ActionStatus itemStatus,
                                  OfferActionState actionState) {
    for (OfferAction action : filterActionListByState(actionList, actionState)) {
      Iterator<ItemWithStatus> itemIterator = notMatchedItems.iterator();
      while (itemIterator.hasNext()) {
        ItemWithStatus item = itemIterator.next();
        if (action.product().originalId().equals(item.id()) &&
            item.status() == itemStatus) {
          itemIterator.remove();
        }
      }
    }
  }

  private Iterable<OfferAction> notApproved(List<OfferAction> actionList) {
    return filterActionListByState(actionList, OfferActionState.NOT_APPROVED);
  }

  private Iterable<OfferAction> approved(List<OfferAction> actionList) {
    return filterActionListByState(actionList, OfferActionState.APPROVED);
  }

  private Iterable<OfferAction> canceled(List<OfferAction> actionList) {
    return filterActionListByState(actionList, OfferActionState.CANCELED);
  }

  private Iterable<OfferAction> filterActionListByState(
      List<OfferAction> actionList, OfferActionState state) {
    ImmutableList.Builder<OfferAction> result = ImmutableList.builder();
    for (OfferAction action : actionList) {
      if (action.state() == state) result.add(action);
    }
    return result.build();

  }
}
