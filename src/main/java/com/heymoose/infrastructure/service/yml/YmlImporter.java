package com.heymoose.infrastructure.service.yml;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.PayMethod;
import com.heymoose.domain.offer.SubOffer;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
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

  protected static class NoInfoException extends Exception {

    public NoInfoException() { }

    public NoInfoException(String msg) {
      super(msg);
    }

  }

  private static final Logger log = LoggerFactory.getLogger(YmlImporter.class);



  private final Repo repo;

  public YmlImporter(Repo repo) {
    this.repo = repo;
  }

  public void doImport(InputSupplier<? extends InputStream> input,
                       Long parentOfferId) {
    Transaction tx = repo.session().getTransaction();
    if (!tx.isActive())
      tx.begin();
    try {
      doImportInsideTransaction(input, parentOfferId);
      tx.commit();
    } catch (Exception e) {
      tx.rollback();
      throw new RuntimeException(e);
    }
  }

  private void doImportInsideTransaction(InputSupplier<? extends InputStream> input,
                                         Long parentOfferId) {
    InputStream inputStream = null;
    YmlCatalog catalog;
    try {
      inputStream = input.getInput();
      JAXBContext context = JAXBContext.newInstance(YmlCatalog.class);

      /* Code below is needed to ignore dtd in yml */
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setFeature("http://apache.org/xml/features/validation/schema", false);
      spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      XMLReader xmlReader = spf.newSAXParser().getXMLReader();
      InputSource inputSource = new InputSource(inputStream);
      SAXSource source = new SAXSource(xmlReader, inputSource);

      Unmarshaller unmarshaller = context.createUnmarshaller();
      catalog = (YmlCatalog) unmarshaller.unmarshal(source);
      log.info("{} categories found.",
          catalog.getShop().getCategories().getCategory().size());
      log.info("{} products found.",
          catalog.getShop().getOffers().getOffer().size());
    } catch (Exception e) {
      log.error("Error occurred during YML parsing.", e);
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }

    com.heymoose.domain.offer.Offer parentOffer =
        repo.get(com.heymoose.domain.offer.Offer.class, parentOfferId);
    parentOffer.setExclusive(true);
    repo.put(parentOffer);


    for (Offer catalogOffer : catalog.getShop().getOffers().getOffer()) {
      String productName = getOfferTitle(catalogOffer);
      String code = getOfferCode(catalogOffer);
      SubOffer subOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parentId = ? and code = ?",
          parentOfferId, code);
      if (subOffer == null)
        subOffer = new SubOffer();

      BigDecimal percent = null;
      BigDecimal cost = null;
      CpaPolicy cpaPolicy = getCpaPolicy(catalogOffer, catalog);
      Preconditions.checkNotNull(cpaPolicy,
          String.format("cpaPolicy is null for item: %s - %s",
              code, productName));
      try {
        switch (cpaPolicy) {
          case FIXED:
            cost = getCost(catalogOffer, catalog);
            break;
          case PERCENT:
            percent = getPercent(catalogOffer, catalog);
            break;
        }
      } catch (NoInfoException e) {
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
        isExclusive = isExclusive(catalogOffer, catalog);
      } catch (NoInfoException e) {
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


  protected String getOfferCode(Offer catalogOffer) {
    return catalogOffer.getId();
  }

  protected String getOfferTitle(Offer offer) {
    List<Object> l = offer
        .getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids();
    for (Object o : l) {
      if (o instanceof Name) {
        return ((Name) o).getvalue();
      }
      if (o instanceof Model) {
        return ((Model) o).getvalue();
      }
    }
    return offer.getDescription();
  }

  /**
   * Should return cpa policy for specific item. Can not return null.
   * @param catalogOffer item description
   * @param catalog whole catalog
   * @return cpa policy for item
   */
  protected abstract CpaPolicy getCpaPolicy(Offer catalogOffer, YmlCatalog catalog);
  protected abstract BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException;
  protected abstract BigDecimal getCost(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException;
  protected abstract boolean isExclusive(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException;

}
