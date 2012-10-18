package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.infrastructure.service.processing.ActionProcessor;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public abstract class ItemListActionDataImporter
    extends ActionDataImporterBase<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(ItemListActionDataImporter.class);

  protected final ActionProcessor processor;

  public ItemListActionDataImporter(Repo repo,
                                    OfferActions actions,
                                    ActionProcessor processor) {
    super(repo, actions);
    this.processor = processor;
  }

  @Override
  protected List<OfferAction> process(ItemListActionData payment,
                                      Offer parentOffer, Token token) {
    ImmutableList.Builder<OfferAction> actionList = ImmutableList.builder();
    for (ItemListActionData.Item item : payment.itemList()) {
      BaseOffer productOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOffer.id(), item.id());
      if (productOffer == null) {
        log.warn("Product with code '{}' does not present in our db! " +
            "Parent offer: '{}'. Using default.", item.id(), parentOffer.id());
        productOffer = parentOffer;
      }
      BigDecimal price = namePrice(item, productOffer);
      if (price == null || price.signum() < 1) {
        log.warn("Can't calculate price for item with code: {}, offer: {}." +
            "Skipping..", item.id(), productOffer.id());
        continue;
      }
      for (int i = 1; i <= item.quantity(); i++) {
        log.info("Adding conversion for offer '{}' code '{}' price '{}'",
            new Object[] {
                productOffer.id() ,
                productOffer.code(),
                price });
        ProcessableData data = new ProcessableData()
            .setToken(token)
            .setTransactionId(payment.transactionId())
            .setPrice(price)
            .setOffer(productOffer);
        OfferAction action = processor.process(data);
        actionList.add(action);
      }
    }
    return actionList.build();
  }
  protected abstract BigDecimal namePrice(ItemListActionData.Item item,
                                          BaseOffer parentOffer);
}
