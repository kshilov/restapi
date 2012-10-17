package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.heymoose.domain.action.FixPriceActionData;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.service.processing.FixActionProcessor;
import com.heymoose.infrastructure.service.processing.ProcessableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class FixPriceActionDataImporter
    extends ActionDataImporterBase<FixPriceActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(FixPriceActionData.class);

  private FixActionProcessor processor;
  @Inject
  public FixPriceActionDataImporter(Repo repo,
                                    OfferActions actions,
                                    FixActionProcessor processor) {
    super(repo, actions);
    this.processor = processor;
  }

  @Override
  protected List<OfferAction> process(FixPriceActionData actionData,
                                      Offer parentOffer) {
    BaseOffer offer = repo.byHQL(Offer.class,
        "from Offer where id = ? and code = ?",
        parentOffer.id(), actionData.offerCode());
    if (offer == null) {
      offer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOffer.id(), actionData.offerCode());
    }
    if (offer == null) {
      log.warn("Offer with code '{}' and parent '{}'. " +
          "Skipping import...",
          actionData.offerCode(), parentOffer.id());
      return ImmutableList.of();
    }
    if (offer.cpaPolicy() == null || offer.cpaPolicy() != CpaPolicy.FIXED) {
      log.warn("Not fixed-price offer '{}'! Skipping..", offer.id());
    }
    log.info("Adding conversion with fix revenue for offer '{}' - '{}'",
        offer.id(), offer.title());
    ProcessableData data = ProcessableData
        .copyActionData(actionData)
        .setOffer(offer);
    return ImmutableList.of(processor.process(data));
  }

}
