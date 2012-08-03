package com.heymoose.infrastructure.service.delikateska;

import com.google.inject.Inject;
import com.heymoose.domain.action.ActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.service.action.ActionDataImporter;

import java.math.BigDecimal;

public class DelikateskaDataImporter extends ActionDataImporter {

  @Inject
  public DelikateskaDataImporter(Repo repo,
                                 Tracking tracking,
                                 OfferActions actions) {
    super(repo, tracking, actions);
  }

  @Override
  protected BigDecimal namePrice(ActionData.Item item, BaseOffer offer) {
    return item.price().multiply(new BigDecimal(item.quantity()));
  }
}
