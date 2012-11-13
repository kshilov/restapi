package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.Item;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.processing.ActionProcessor;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@Singleton
public class ItemListProductImporter
    implements ActionDataImporter<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(ItemListProductImporter.class);

  protected final ActionProcessor processor;
  private final Repo repo;
  private final OfferActions actions;

  @Inject
  public ItemListProductImporter(Repo repo, OfferActions actions,
                                 ActionProcessor processor) {
    this.processor = processor;
    this.repo = repo;
    this.actions = actions;
  }

  @Override
  @Transactional
  public void doImport(List<ItemListActionData> actionList, Long parentOfferId) {
    log.info("Starting import for {} actions.", actionList.size());
    for (ItemListActionData action : actionList) {
      doImport(action, parentOfferId);
    }
  }

  @Override
  public void doImport(ItemListActionData payment, Long parentOfferId) {
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

    if (offerActionList.size() == 0) {
      Offer parentOffer = repo.get(Offer.class, parentOfferId);
      if (parentOffer == null) {
        log.warn("Unknown parent offer: {}", parentOfferId);
        throw new IllegalArgumentException("Unknown parent offer id " +
            parentOfferId);
      }
      offerActionList = process(payment, parentOffer, token);
    } else {
      log.info("Transaction '{}' was already converted, offerActions='{}'. ",
          payment.transactionId(), idList(offerActionList));
    }
    if (payment.status().equals(ActionStatus.CANCELED)) {
      for (OfferAction action : offerActionList) {
        log.info("Cancelling action '{}'.", action.id());
        actions.cancel(action);
      }
    }
  }

  protected List<OfferAction> process(ItemListActionData payment,
                                      Offer parentOffer, Token token) {
    ImmutableList.Builder<OfferAction> actionList = ImmutableList.builder();
    for (Item item : payment.itemList()) {
      Product product = repo.byHQL(Product.class,
          "from Product where offer = ? and originalId = ?",
          parentOffer, item.id());
      BigDecimal price = item.price();
      if (price == null && product != null) price = product.price();
      for (int i = 1; i <= item.quantity(); i++) {
        log.info("Adding conversion for offer '{}' product {} price {}",
            new Object[] {
                parentOffer.id(),
                product, price });
        ProcessableData data = new ProcessableData()
            .setToken(token)
            .setTransactionId(payment.transactionId())
            .setPrice(price)
            .setOffer(parentOffer)
            .setProduct(product);
        try {
          processor.process(data);
          if (data.offerAction() != null) actionList.add(data.offerAction());
        } catch (Exception ex) {
          log.warn("Exception during processing. data: {}", data);
          log.error("Processing exception", ex);
        }
      }
    }
    return actionList.build();
  }


  private List<Long> idList(Iterable<? extends IdEntity> entityList) {
    ImmutableList.Builder<Long> idList = ImmutableList.builder();
    for (IdEntity entity : entityList) {
      idList.add(entity.id());
    }
    return idList.build();
  }

}
