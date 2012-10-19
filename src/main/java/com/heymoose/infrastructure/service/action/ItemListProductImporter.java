package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.processing.ActionProcessor;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public final class ItemListProductImporter
    extends ActionDataImporterBase<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(ItemListProductImporter.class);

  protected ActionProcessor processor;

  public ItemListProductImporter(Repo repo, OfferActions actions,
                                 ActionProcessor processor) {
    super(repo, actions);
    this.processor = processor;
  }

  @Override
  protected List<OfferAction> process(ItemListActionData payment,
                                      Offer parentOffer, Token token) {
    ImmutableList.Builder<OfferAction> actionList = ImmutableList.builder();
    for (ItemListActionData.Item item : payment.itemList()) {
      Product product = repo.byHQL(Product.class,
          "from Product where offer = ? and originalId = ?",
          parentOffer, item.id());
      BigDecimal price = item.price();
      if (price == null) {
        if (product == null) {
          log.warn("Can't price item: {}. Skipping..", item);
          continue;
        } else {
          price = product.price();
        }
      }
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
        OfferAction action = processor.process(data);
        actionList.add(action);
      }
    }
    return actionList.build();
  }
}
