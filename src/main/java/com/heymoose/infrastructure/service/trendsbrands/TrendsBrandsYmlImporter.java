package com.heymoose.infrastructure.service.trendsbrands;

import com.google.common.base.Strings;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.TypePrefix;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlImporter;

import java.math.BigDecimal;
import java.util.List;

public final class TrendsBrandsYmlImporter extends YmlImporter {

  private static final BigDecimal EXCLUSIVE_LIMIT = new BigDecimal(6000);
  private static final BigDecimal EXCLUSIVE_COST = new BigDecimal(1950);
  private static final BigDecimal REGULAR_COST = new BigDecimal(650);

  public TrendsBrandsYmlImporter(Repo repo) {
    super(repo);
  }

  @Override
  protected CpaPolicy getCpaPolicy(Offer catalogOffer, YmlCatalog catalog) {
    return CpaPolicy.FIXED;
  }

  @Override
  protected BigDecimal getPercent(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    return null;
  }

  @Override
  protected BigDecimal getCost(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    BigDecimal price = new BigDecimal(catalogOffer.getPrice());
    if (isExclusive(catalogOffer, catalog)) {
      return EXCLUSIVE_COST;
    }
    return REGULAR_COST;
  }

  @Override
  protected boolean isExclusive(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    BigDecimal price = new BigDecimal(catalogOffer.getPrice());
    return price.compareTo(EXCLUSIVE_LIMIT) > 0;
  }

  @Override
  protected String getOfferCode(Offer catalogOffer) {
    return catalogOffer.getGroupId();
  }

  @Override
  protected String getOfferTitle(Offer offer) {
    List<Object> bunchOfStuff = offer.getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids();
    String typePrefix = "";
    String vendor = "";
    String model = "";
    for (Object element : bunchOfStuff) {
      if (element instanceof TypePrefix) {
        typePrefix = ((TypePrefix) element).getvalue();
      }
      if (element instanceof Vendor) {
        vendor = ((Vendor) element).getvalue();
      }
      if (element instanceof Model) {
        model = ((Model) element).getvalue();
      }
    }
    return new StringBuilder()
        .append(typePrefix)
        .append(' ')
        .append(vendor)
        .append(' ')
        .append(model)
        .toString();
  }
}
