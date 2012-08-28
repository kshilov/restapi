package com.heymoose.infrastructure.service.topshop;

import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.service.action.ItemListActionDataImporter;

import javax.inject.Inject;
import java.math.BigDecimal;

public class TopShopDataImporter extends ItemListActionDataImporter {

  @Inject
  public TopShopDataImporter(Repo repo, Tracking tracking,
                             OfferActions actions) {
    super(repo, tracking, actions);
  }

  protected BigDecimal namePrice(ItemListActionData.Item item, BaseOffer offer) {
    return offer.itemPrice();
  }
}
