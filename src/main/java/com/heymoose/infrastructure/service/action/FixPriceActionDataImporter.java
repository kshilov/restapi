package com.heymoose.infrastructure.service.action;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.heymoose.domain.action.FixPriceActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Tracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

public class FixPriceActionDataImporter
    extends ActionDataImporterBase<FixPriceActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(FixPriceActionData.class);

  @Inject
  public FixPriceActionDataImporter(Repo repo, Tracking tracking,
                                    OfferActions actions) {
    super(repo, tracking, actions);
  }

  @Override
  protected Map<BaseOffer, Optional<Double>> extractOffers(
      FixPriceActionData actionData, Long parentOfferId) {
    BaseOffer offer = repo.byHQL(Offer.class,
        "from Offer where id = ? and code = ?",
        parentOfferId, actionData.offerCode());
    if (offer == null) {
      offer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOfferId, actionData.offerCode());
      }
    if (offer == null) {
      log.warn("Offer with code '{}' and parent '{}'. " +
          "Skipping import...",
          actionData.offerCode(), parentOfferId);
      return ImmutableMap.of();
    }
    if (offer.cpaPolicy() == null || offer.cpaPolicy() != CpaPolicy.FIXED) {
      log.warn("Not fixed-price offer '{}'! Skipping..", offer.id());
    }
    log.info("Adding conversion with fix revenue for offer '{}' - '{}'",
        offer.id(), offer.title());
    return ImmutableMap.of(offer, Optional.<Double>absent());
  }
}
