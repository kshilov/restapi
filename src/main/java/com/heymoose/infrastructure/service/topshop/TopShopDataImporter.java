package com.heymoose.infrastructure.service.topshop;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.name.Named;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.statistics.Token;
import com.heymoose.domain.statistics.Tracking;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TopShopDataImporter {

  private static final Logger log =
      LoggerFactory.getLogger(TopShopDataImporter.class);

  private final Repo repo;
  private final Tracking tracking;
  private final Map<String, Long> topShopOfferMap;

  @Inject
  public TopShopDataImporter(Repo repo, Tracking tracking,
                             @Named("top-shop-offer-map")
                             Map<String, Long> topShopOfferMap) {
    this.repo = repo;
    this.tracking = tracking;
    this.topShopOfferMap = topShopOfferMap;
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
    for (Map.Entry<String, BigDecimal> itemPrice : payment.itemPriceMap().entrySet()) {
      Offer offer = repo.get(Offer.class, topShopOfferMap.get(itemPrice.getKey()));
      log.info("Adding conversion for offer '{}' price '{}'",
          offer.id(), itemPrice.getValue());
      offerMap.put(offer, Optional.of(itemPrice.getValue().doubleValue()));
    }
    tracking.trackConversion(token, payment.transactionId(), offerMap.build());
  }
}
