package com.heymoose.infrastructure.service.action;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.domain.action.OfferActions;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.BaseOffer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.domain.statistics.Tracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public abstract class ItemListActionDataImporter
    extends ActionDataImporterBase<ItemListActionData> {

  private static final Logger log =
      LoggerFactory.getLogger(ItemListActionDataImporter.class);

  public ItemListActionDataImporter(Repo repo, Tracking tracking,
                                    OfferActions actions) {
    super(repo, tracking, actions);
  }


  @Override
  protected final Multimap<BaseOffer, Optional<Double>> extractOffers(
      ItemListActionData payment, Long parentOfferId) {

    if (payment.itemList().isEmpty()) {
      log.warn("Action with no items! TransactionId: {}",
          payment.transactionId());
    }

    ImmutableMultimap.Builder<BaseOffer, Optional<Double>> offerMap =
        ImmutableMultimap.builder();
    for (ItemListActionData.Item item : payment.itemList()) {
      BaseOffer productOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parent_id = ? and code = ?",
          parentOfferId, item.id());
      if (productOffer == null) {
        log.warn("Product with code '{}' does not present in our db! " +
            "Parent offer: '{}'. Skipping..", item.id(), parentOfferId);
        continue;
      }
      BigDecimal price = namePrice(item, productOffer);
      for (int i = 1; i <= item.quantity(); i++) {
        log.info("Adding conversion for offer '{}' code '{}' price '{}'",
            new Object[] {
                productOffer.id() ,
                productOffer.code(),
                price });
        offerMap.put(productOffer, Optional.of(price.doubleValue()));
      }
    }
    return offerMap.build();
  }

  protected abstract BigDecimal namePrice(ItemListActionData.Item item,
                                          BaseOffer offer);
}
