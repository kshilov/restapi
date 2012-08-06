package com.heymoose.infrastructure.service.topshop;

import com.google.inject.name.Named;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.service.action.ActionDataImporter;

import javax.inject.Inject;
import java.math.BigDecimal;

public class TopShopDataImporter extends ActionDataImporter {

  @Inject
  public TopShopDataImporter(Repo repo, Tracking tracking,
                             OfferActions actions) {
    super(repo, tracking, actions);
  }

  protected BigDecimal namePrice(ActionData.Item item, BaseOffer offer) {
    return offer.cost();
  }
}
