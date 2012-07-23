package com.heymoose.infrastructure.service.topshop;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class TopShopDataImporter {

  private static final Logger log =
      LoggerFactory.getLogger(TopShopDataImporter.class);

  private final Repo repo;
  private final Tracking tracking;

  @Inject
  public TopShopDataImporter(Repo repo, Tracking tracking) {
    this.repo = repo;
    this.tracking = tracking;
  }

  @Transactional
  public void doImport(List<TopShopPaymentData> paymentList) {
    for (TopShopPaymentData payment : paymentList) {
      doImport(payment);
    }
  }

  private void doImport(TopShopPaymentData payment) {
    Token token = repo.byHQL(Token.class,
        "from Token where value = ?", payment.token());
    if (token == null) {
      log.warn("Token {} not found, skiping", payment.token());
      return;
    }

    // check whether token was not tracked already
    OfferAction offerAction = repo.byHQL(OfferAction.class,
        "from OfferAction where token = ?", token);
    if (offerAction != null) {
      log.warn("Token {} was already converted, offerAction={}, skiping",
          payment.token(), offerAction.id());
      return;
    }

    ImmutableMap.Builder<BaseOffer, Optional<Double>> offerMap =
        ImmutableMap.builder();
    for (Long itemId : payment.items()) {
      SubOffer topshopOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where code = ?", itemId.toString());
      List<Offer> all = repo.allByHQL(Offer.class, "from Offer");
      log.info("Adding conversion for offer '{}' price '{}'",
          topshopOffer.id(), topshopOffer.cost());
      offerMap.put(topshopOffer, Optional.of(topshopOffer.cost().doubleValue()));
    }
    tracking.trackConversion(token, payment.transactionId(), offerMap.build());
  }
}
