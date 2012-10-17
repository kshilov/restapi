package com.heymoose.infrastructure.service.action;

import com.google.inject.Inject;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.infrastructure.service.processing.PercentActionProcessor;

import java.math.BigDecimal;

public class BasicItemListDataImporter extends ItemListActionDataImporter {

  @Inject
  public BasicItemListDataImporter(Repo repo,
                                   OfferActions actions,
                                   PercentActionProcessor processor) {
    super(repo, actions, processor);
  }

  @Override
  protected BigDecimal namePrice(ItemListActionData.Item item, BaseOffer offer) {
    return item.price();
  }
}
