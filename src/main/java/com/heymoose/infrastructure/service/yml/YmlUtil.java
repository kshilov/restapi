package com.heymoose.infrastructure.service.yml;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public final class YmlUtil {

  private static final Logger log = LoggerFactory.getLogger(YmlUtil.class);

  private YmlUtil() { }

  public static YmlCatalog loadYml(String url) {
    try {
      return loadYml(new URL(url));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public static YmlCatalog loadYml(URL url) {
    InputSupplier<InputStream> inputSupplier =
        Resources.newInputStreamSupplier(url);
    return parse(inputSupplier);
  }

  public static YmlCatalog parse(InputSupplier<? extends InputStream> input) {
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
      return (YmlCatalog) unmarshaller.unmarshal(source);
    } catch (Exception e) {
      log.error("Error occurred during YML parsing.", e);
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  public static Map<Class<?>, String> extractOptionalFields(
      Offer offer, Class<?>... clzList) {
    Map<Class<?>, String> map = Maps.newHashMapWithExpectedSize(clzList.length);
    for (Class<?> clz : clzList) {
      map.put(clz, "");
    }
    List<Object> fieldList = offer.getTypePrefixOrVendorOrVendorCodeOrModelOrProviderOrTarifplanOrAuthorOrNameOrPublisherOrSeriesOrYearOrISBNOrVolumeOrPartOrLanguageOrBindingOrPageExtentOrTableOfContentsOrPerformedByOrPerformanceTypeOrStorageOrFormatOrRecordingLengthOrArtistOrTitleOrMediaOrStarringOrDirectorOrOriginalNameOrCountryOrWorldRegionOrRegionOrDaysOrDataTourOrHotelStarsOrRoomOrMealOrIncludedOrTransportOrPriceMinOrPriceMaxOrOptionsOrPlaceOrHallOrHallPartOrDateOrIsPremiereOrIsKids();
    try {
      for (Object field : fieldList) {
        for (Class<?> clz : clzList) {
          if (clz.isInstance(field)) {
            map.put(clz, clz.getMethod("getvalue").invoke(field).toString());
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return map;
  }

  public static String extractOptionalField(Offer offer, Class<?> clz) {
    return extractOptionalFields(offer, clz).get(clz);
  }

  public static String titleFor(Offer offer, Class<?>... clzList) {
    Map<Class<?>, String> map = extractOptionalFields(offer, clzList);
    StringBuilder builder = new StringBuilder();
    for (Class<?> clz : clzList) {
      builder.append(map.get(clz));
      builder.append(' ');
    }
    builder.setLength(builder.length() - 1);
    return builder.toString();
  }
}
