package com.heymoose.infrastructure.server;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.heymoose.domain.offer.CpaPolicy;
import com.heymoose.domain.offer.SubOffer;
import com.heymoose.infrastructure.context.CommonModule;
import com.heymoose.infrastructure.context.ProductionModule;
import com.heymoose.infrastructure.context.SettingsModule;
import com.heymoose.infrastructure.service.yml.Category;
import com.heymoose.infrastructure.service.yml.Name;
import com.heymoose.infrastructure.service.yml.Offer;
import com.heymoose.infrastructure.service.yml.YmlCatalog;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TopShopYmlImport {

  private static class TopShopOffersModule extends AbstractModule {

    @Override
    protected void configure() {
    }

  }

  private static final Logger log = LoggerFactory.getLogger(TopShopYmlImport.class);

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

  public static void main(String... args) throws Exception {
    Injector injector = Guice.createInjector(
        new SettingsModule(),
        new CommonModule(),
        new ProductionModule(),
        new TopShopOffersModule());
    if (Strings.isNullOrEmpty(args[0])) {
      System.out.println("YML file not specified.");
      System.exit(2);
    }
    Properties properties = injector.getInstance(
        Key.get(Properties.class, Names.named("settings")));
    Long parentOfferId = Long.valueOf(properties.get("topshop.offer.id").toString());

    JAXBContext context = JAXBContext.newInstance(YmlCatalog.class);
    Reader reader = Files.newReader(new File(args[0]), Charset.forName("utf8"));
    Unmarshaller unmarshaller = context.createUnmarshaller();
    YmlCatalog cat = (YmlCatalog) unmarshaller.unmarshal(reader);
    log.info("{} categories found.", cat.getShop().getCategories().getCategory().size());
    log.info("{} products found.", cat.getShop().getOffers().getOffer().size());
    Map<Integer, Integer> childToParentCategory = mapChildToParent(cat);

    Session session = injector.getInstance(Session.class);
    Transaction tx = session.beginTransaction();
    com.heymoose.domain.offer.Offer parentOffer = (com.heymoose.domain.offer.Offer)
        session.get(com.heymoose.domain.offer.Offer.class, parentOfferId);
    try {
      for (Offer catalogOffer : cat.getShop().getOffers().getOffer()) {
        String productCategoryString = catalogOffer.getCategoryId().get(0).getvalue();
        if (Strings.isNullOrEmpty(productCategoryString)) {
          log.info("Category does not present for product {} - {}. Skipping.",
              catalogOffer.getId(), name(catalogOffer));
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
        SubOffer subOffer = mapYmlOfferToHeymooseSubOffer(
            parentOffer,
            CATEGORY_PERCENT_MAP.get(parentCategory),
            catalogOffer);
        session.saveOrUpdate(subOffer);
        log.info("Sub offer for product: {} - {}. Saved with id: {}",
            new Object[] { catalogOffer.getId(), subOffer.title(), subOffer.id() });
      }
      tx.commit();
    } catch (Exception e) {
      log.error("Import failed.", e);
      tx.rollback();
      throw new RuntimeException(e);
    }
  }

  private static SubOffer mapYmlOfferToHeymooseSubOffer(com.heymoose.domain.offer.Offer parent,
                                                        BigDecimal percent,
                                                        Offer product) {
    return new SubOffer(parent.id(), CpaPolicy.PERCENT,
        new BigDecimal(product.getPrice()), null, percent, name(product),
        false, true, product.getId(), parent.holdDays());
  }

  private static String name(Offer offer) {
    List<Object> l = offer.getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids();
    for (Object o : l) {
      if (o instanceof Name) {
        return ((Name) o).getvalue();
      }
    }
    return offer.getDescription();
  }

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
}
