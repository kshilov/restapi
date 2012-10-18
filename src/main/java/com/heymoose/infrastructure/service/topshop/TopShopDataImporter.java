package com.heymoose.infrastructure.service.topshop;

import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.service.action.ItemListActionDataImporter;
import com.heymoose.infrastructure.service.processing.ActionProcessor;

import javax.inject.Inject;
import java.math.BigDecimal;

public class TopShopDataImporter extends ItemListActionDataImporter {

  @Inject
  public TopShopDataImporter(Repo repo,
                             OfferActions actions,
                             ActionProcessor processor) {
    super(repo, actions, processor);
  }

  protected BigDecimal namePrice(ItemListActionData.Item item, BaseOffer offer) {
    if (offer instanceof Offer) {
      return null;
    }
    return offer.itemPrice();
  }
}
