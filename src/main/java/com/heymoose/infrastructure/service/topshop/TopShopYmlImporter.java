package com.heymoose.infrastructure.service.topshop;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.yml.Category;
import com.heymoose.infrastructure.service.yml.Name;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopShopYmlImporter {

  private static final Logger log = LoggerFactory
      .getLogger(TopShopYmlImporter.class);

  private static final BigDecimal TWO = new BigDecimal(2);
  private static final BigDecimal SIX = new BigDecimal(6);
  private static final BigDecimal EIGHT = new BigDecimal(8);
  private static final BigDecimal FIFTEEN = new BigDecimal(15);
  private static final Map<Integer, BigDecimal> CATEGORY_PERCENT_MAP =
      ImmutableMap.<Integer, BigDecimal>builder()
          .put(12, FIFTEEN) // тв товары
          .put(17, FIFTEEN) // дормео
          .put(19, FIFTEEN) // космодиск
          .put(18, FIFTEEN) // делимано
          .put(1, TWO)      // бытовая техника
          .put(2, TWO)      // электроника
          .put(3, TWO)      // товары для дома
          .put(4, SIX)      // дача, сад, огород
          .put(5, SIX)      // красота и здоровье
          .put(9, EIGHT)    // спорт и отдых
          .put(6, EIGHT)    // товары для детей
          .put(7, EIGHT)    // товары для автомобиля
          .put(8, SIX)      // одежда и обувь
          .put(10, SIX)     // подарки, украшения
          .build();
  private static final Set<Integer> EXCLUSIVE_CATEGORIES =
      ImmutableSet.of(12, 17, 18, 19);

  private static Map<Integer, Integer> mapChildToParent(YmlCatalog catalog) {
    HashMap<Integer, Integer> parentMap = Maps.newHashMap();
    for (Category category : catalog.getShop().getCategories().getCategory()) {
      Integer parentId = null;
      if (category.getParentId() != null) {
        parentId = Integer.valueOf(category.getParentId());
      }
      parentMap.put(Integer.valueOf(category.getId()), parentId);
    }
    boolean changed = true;
    // map child to the top level parent
    outer:
    while (changed) {
      for (Integer categoryId : parentMap.keySet()) {
        Integer parentId = parentMap.get(categoryId);
        // if parent of the category has parents itself
        if (parentId != null && parentMap.get(parentId) != null) {
          parentMap.put(categoryId, parentMap.get(parentId));
          continue outer;
        }
      }
      changed = false;
    }
    return parentMap;
  }


  private static String name(
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

  @Inject
  public TopShopYmlImporter(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public void doImport(InputSupplier<? extends Reader> input, Long parentOfferId) {
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

    Map<Integer, Integer> childToParentCategory = mapChildToParent(catalog);
    Offer parentOffer = repo.get(Offer.class, parentOfferId);
    parentOffer.setExclusive(true);
    repo.put(parentOffer);


    for (com.heymoose.infrastructure.service.yml.Offer catalogOffer :
        catalog.getShop().getOffers().getOffer()) {
      String productCategoryString = catalogOffer.getCategoryId().get(0)
          .getvalue();
      String productName = name(catalogOffer);
      if (Strings.isNullOrEmpty(productCategoryString)) {
        log.info("Category does not present for product {} - {}. Skipping.",
            catalogOffer.getId(), productName);
        continue;
      }
      Integer productCategory = Integer.valueOf(productCategoryString);
      Integer parentCategory = childToParentCategory.get(productCategory);
      if (parentCategory == null) {
        parentCategory = productCategory;
      }
      if (CATEGORY_PERCENT_MAP.get(parentCategory) == null) {
        log.info("Category {} is not mapped. Skipping.", parentCategory);
        continue;
      }
      SubOffer subOffer = repo.byHQL(SubOffer.class,
          "from SubOffer where parentId = ? and code = ?",
          parentOfferId, catalogOffer.getId());
      if (subOffer == null)
        subOffer = new SubOffer();
      subOffer.setParentId(parentOffer.id())
              .setCode(catalogOffer.getId())
              .setCost(new BigDecimal(catalogOffer.getPrice()))
              .setTitle(productName)
              .setPercent(CATEGORY_PERCENT_MAP.get(parentCategory))
              .setCpaPolicy(CpaPolicy.PERCENT)
              .setAutoApprove(false)
              .setReentrant(true)
              .setHoldDays(parentOffer.holdDays());
      if (EXCLUSIVE_CATEGORIES.contains(parentCategory)) {
        subOffer.setExclusive(true);
      }
      repo.put(subOffer);
      log.info("Sub offer for product: {} - {}. Saved with id: {}",
          new Object[]{catalogOffer.getId(), subOffer.title(), subOffer.id()});
    }
  }
}
