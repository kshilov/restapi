package com.heymoose.infrastructure.service.delikateska;

import com.google.inject.Inject;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.service.action.ItemListActionDataImporter;

import java.math.BigDecimal;

public class DelikateskaDataImporter extends ItemListActionDataImporter {

  @Inject
  public DelikateskaDataImporter(Repo repo,
                                 Tracking tracking,
                                 OfferActions actions) {
    super(repo, tracking, actions);
  }

  @Override
  protected BigDecimal namePrice(ItemListActionData.Item item, BaseOffer offer) {
    return item.price();
  }
}
