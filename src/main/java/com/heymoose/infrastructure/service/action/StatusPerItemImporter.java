package com.heymoose.infrastructure.service.action;

import com.heymoose.domain.action.OfferAction;
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

import java.util.List;

public class StatusPerItemImporter
    implements ActionDataImporter<StatusPerItemActionData> {

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
  public void doImport(StatusPerItemActionData action, Long parentOfferId) {

    Token token = tokens.byValue(action.token());
    Offer offer = offers.offerById(parentOfferId);

    for (StatusPerItemActionData.ItemWithStatus item : action.itemList()) {
      Product product = products.byOriginalId(parentOfferId, item.id());
      List<OfferAction> actionList = actions.listProductActions(
          token,
          action.transactionId(),
          product);
      if (actionList.size() > 0) continue;
      ProcessableData data = new ProcessableData();
      data.setProduct(product)
          .setPrice(item.price())
          .setToken(token)
          .setTransactionId(action.transactionId())
          .setOffer(offer);
      processor.process(data);
    }

  }
}
