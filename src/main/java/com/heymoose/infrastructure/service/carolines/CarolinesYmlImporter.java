package com.heymoose.infrastructure.service.carolines;

import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.infrastructure.service.yml.Model;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.Vendor;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import com.heymoose.infrastructure.service.yml.YmlImporter;

import java.math.BigDecimal;
import java.util.List;

public final class CarolinesYmlImporter extends YmlImporter {

  public static final BigDecimal EXCLUSIVE_COST = new BigDecimal(300);
  public static final BigDecimal REGULAR_COST = new BigDecimal(240);

  private final List<String> exclusiveList;

  public CarolinesYmlImporter(Repo repo, List<String> exclusiveList) {
    super(repo);
    this.exclusiveList = exclusiveList;
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
    if (isExclusive(catalogOffer, catalog)) {
      return EXCLUSIVE_COST;
    }
    return REGULAR_COST;
  }

  @Override
  protected boolean isExclusive(Offer catalogOffer, YmlCatalog catalog)
      throws NoInfoException {
    return exclusiveList.contains(catalogOffer.getId());
  }

  @Override
  protected String getOfferTitle(Offer offer) {
    List<Object> bunchOfStuff = offer.getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids();
    String vendor = "";
    String model = "";
    for (Object element : bunchOfStuff) {
      if (element instanceof Vendor) {
        vendor = ((Vendor) element).getvalue();
      }
      if (element instanceof Model) {
        model = ((Model) element).getvalue();
      }
    }
    return new StringBuilder()
        .append(vendor)
        .append(' ')
        .append(model)
        .toString();
  }
}
