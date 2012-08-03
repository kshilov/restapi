package com.heymoose.infrastructure.service.yml;

import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.persistence.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.List;

/**
 * Base class, that is responsible for importing yml data, by saving each
 * product as suboffer.
 *
 * To extend it, you should provide
 * <ul>
 *  <li>
 *  {@link #getPercent(Offer, YmlCatalog)} implementation.
 *  This function should return value of percent, advertiser will pay for
 *  bought product.
 *  </li>
 *  <li>
 *    {@link #isExclusive(Offer, YmlCatalog)} implementation should return
 *    true if the product has special conditions.
 *  </li>
 *</ul>
 */
public abstract class YmlImporter {

  private static final Logger log = LoggerFactory.getLogger(YmlImporter.class);


  protected static String name(
      com.heymoose.infrastructure.service.yml.Offer offer) {
    List<Object> l = offer
        .getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids();
    for (Object o : l) {
      if (o instanceof Name) {
        return ((Name) o).getvalue();
      }
    }
    return offer.getDescription();
  }

  private final Repo repo;

  public YmlImporter(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public void doImport(InputSupplier<? extends Reader> input,
                       Long parentOfferId) {
    Reader inputReader = null;
    YmlCatalog catalog;
    try {
      inputReader = input.getInput();
      JAXBContext context = JAXBContext.newInstance(YmlCatalog.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      catalog = (YmlCatalog) unmarshaller.unmarshal(inputReader);
      log.info("{} categories found.",
          catalog.getShop().getCategories().getCategory().size());
      log.info("{} products found.",
          catalog.getShop().getOffers().getOffer().size());
    } catch (Exception e) {
      log.error("Error occurred during YML parsing.", e);
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(inputReader);
    }

    com.heymoose.domain.offer.Offer parentOffer =
        repo.get(com.heymoose.domain.offer.Offer.class, parentOfferId);
    parentOffer.setExclusive(true);
    repo.put(parentOffer);


    for (Offer catalogOffer : catalog.getShop().getOffers().getOffer()) {
      String productName = name(catalogOffer);
      SubOffer subOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parentId = ? and code = ?",
          parentOfferId, catalogOffer.getId());
      if (subOffer == null)
        subOffer = new SubOffer();
      subOffer.setParentId(parentOffer.id())
          .setCode(catalogOffer.getId())
          .setCost(new BigDecimal(catalogOffer.getPrice()))
          .setTitle(productName)
          .setPercent(getPercent(catalogOffer, catalog))
          .setPayMethod(PayMethod.CPA)
          .setCpaPolicy(CpaPolicy.PERCENT)
          .setAutoApprove(false)
          .setReentrant(true)
          .setHoldDays(parentOffer.holdDays());
      if (isExclusive(catalogOffer, catalog)) {
        subOffer.setExclusive(true);
      }
      repo.put(subOffer);
      log.info("Sub offer for product: {} - {}. Saved with id: {}",
          new Object[]{catalogOffer.getId(), subOffer.title(), subOffer.id()});
    }
  }

  protected abstract BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog);
  protected abstract boolean isExclusive(Offer catalogOffer, YmlCatalog catalog);

}
