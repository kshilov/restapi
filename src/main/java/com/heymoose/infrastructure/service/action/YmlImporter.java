package com.heymoose.infrastructure.service.action;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalogWrapper;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class YmlImporter {

  private static final Logger log = LoggerFactory.getLogger(YmlImporter.class);

  private final Repo repo;

  @Inject
  public YmlImporter(Repo repo) {
    this.repo = repo;
  }

  public void doImport(YmlCatalogWrapper catalog, Long parentOfferId) {
    Transaction tx = repo.session().getTransaction();
    if (!tx.isActive())
      tx.begin();
    try {
      doImportInsideTransaction(catalog, parentOfferId);
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      throw new RuntimeException(e);
    }
  }

  private void doImportInsideTransaction(YmlCatalogWrapper catalog,
                                         Long parentOfferId) {

    com.heymoose.domain.offer.Offer parentOffer =
        repo.get(com.heymoose.domain.offer.Offer.class, parentOfferId);
    parentOffer.setExclusive(true);
    repo.put(parentOffer);


    for (Offer catalogOffer : catalog.listOffers()) {
      String productName = catalog.getOfferTitle(catalogOffer);
      String code = catalog.getOfferCode(catalogOffer);
      SubOffer subOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parentId = ? and code = ?",
          parentOfferId, code);
      if (subOffer == null)
        subOffer = new SubOffer();

      BigDecimal percent = null;
      BigDecimal cost = null;
      CpaPolicy cpaPolicy = catalog.getCpaPolicy(catalogOffer);
      Preconditions.checkNotNull(cpaPolicy,
          String.format("cpaPolicy is null for item: %s - %s",
              code, productName));
      try {
        switch (cpaPolicy) {
          case FIXED:
            cost = catalog.getCost(catalogOffer);
            break;
          case PERCENT:
            percent = catalog.getPercent(catalogOffer);
            break;
        }
      } catch (YmlCatalogWrapper.NoInfoException e) {
        log.warn("No pricing info in YML for product: {} - {}. Skipping..",
            code, productName);
      }

      subOffer.setParentId(parentOffer.id())
          .setCode(code)
          .setItemPrice(new BigDecimal(catalogOffer.getPrice()))
          .setTitle(productName)
          .setPayMethod(PayMethod.CPA)
          .setCpaPolicy(cpaPolicy)
          .setPercent(percent)
          .setCost(cost)
          .setAutoApprove(false)
          .setReentrant(true)
          .setHoldDays(parentOffer.holdDays());
      boolean isExclusive = false;
      try {
        isExclusive = catalog.isExclusive(catalogOffer);
      } catch (YmlCatalogWrapper.NoInfoException e) {
        log.warn("No info about exclusiveness for product {} - {}. " +
            "Setting exclusive = false..",
            code, productName);
      }
      subOffer.setExclusive(isExclusive);
      repo.put(subOffer);
      log.info("Sub offer for product: {} - {}. Saved with id: {}",
          new Object[]{code, subOffer.title(), subOffer.id()});
    }
  }
}
