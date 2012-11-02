package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

@Singleton
public class ItemListProductImporter
    extends ActionDataImporterBase<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(ItemListProductImporter.class);

  protected final ActionProcessor processor;

  @Inject
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
          actionList.add(data.offerAction());
        } catch (Exception ex) {
          log.warn("Exception during processing. data: {}", data);
          log.error("Processing exception", ex);
        }
      }
    }
    return actionList.build();
  }
}
