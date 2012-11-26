package com.heymoose.infrastructure.service.yml;

import com.heymoose.domain.base.IdEntity;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.site.Site;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.User;
import org.joda.time.DateTime;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Map;

public class YmlWriter {

  private final XMLStreamWriter streamWriter;
  private Iterable<Product> productList;
  private Iterable<ShopCategory> categoryList;
  private User user;
  private String urlMask;
  private Site site;

  public YmlWriter(OutputStream stream) {
    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    try {
      this.streamWriter = outputFactory.createXMLStreamWriter(stream);
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }

  public YmlWriter setProductList(Iterable<Product> productList) {
    this.productList = productList;
    return this;
  }

  public YmlWriter setCategoryList(Iterable<ShopCategory> categoryList) {
    this.categoryList = categoryList;
    return this;
  }

  public void write() throws XMLStreamException {
    streamWriter.writeStartDocument("utf-8", "1.0");
    streamWriter.writeStartElement("yml_catalog");
    streamWriter.writeAttribute("date", DateTime.now().toString());
    streamWriter.writeStartElement("shop");
    writeElement("name", "HeyMoose!");
    writeElement("company", "HeyMoose!");
    writeElement("url", "http://www.heymoose.com");

    writeCurrencies();
    writeCategories();
    writeOffers();

    streamWriter.writeEndElement(); //shop
    streamWriter.writeEndElement(); //yml_catalog
  }

  private void writeCurrencies() throws XMLStreamException {
    streamWriter.writeStartElement("currencies");
    streamWriter.writeStartElement("currency");
    streamWriter.writeAttribute("id", "RUR");
    streamWriter.writeAttribute("rate", "1");
    streamWriter.writeEndElement(); //currency
    streamWriter.writeEndElement(); //currencies
  }

  private void writeCategories() throws XMLStreamException {
    streamWriter.writeStartElement("categories");
    Map<Long, ShopCategory> categoryMap = IdEntity.toMap(categoryList);
    for (ShopCategory category : categoryMap.values()) {
      streamWriter.writeStartElement("category");
      streamWriter.writeAttribute("id", category.id().toString());
      if (category.parentId() != null) {
        streamWriter.writeAttribute("parentId", category.parentId().toString());
      }
      streamWriter.writeCharacters(category.name());
      streamWriter.writeEndElement();
    }
    streamWriter.writeEndElement(); //categories
  }

  private void writeOffers() throws XMLStreamException {
    streamWriter.writeStartElement("offers");
    for (Product product : productList) {
      writeProduct(product);
    }
    streamWriter.writeEndElement(); //offers
  }

  private void writeProduct(Product product) throws XMLStreamException {
    streamWriter.writeStartElement("offer");

    // attributes
    streamWriter.writeAttribute("id", product.id().toString());
    for (Map.Entry<String, String> extra : product.extraInfo().entrySet()) {
      if (extra.getKey().equals("id")) continue;
      streamWriter.writeAttribute(extra.getKey(), extra.getValue());
    }

    // url
    String originalUrlEncoded = "";
    try {
      originalUrlEncoded = URLEncoder.encode(product.url(), "utf-8");
    } catch (UnsupportedEncodingException e) {
    }
    String newUrl = String.format(urlMask,
          product.offer().id(), user.id(), originalUrlEncoded);
    writeElement("url", newUrl);

    BigDecimal price = product.price();
    streamWriter.writeStartElement("price");
    if (price != null) streamWriter.writeCharacters(price.toString());
    streamWriter.writeEndElement();
    writeElement("currencyId", "RUR");
//    writeProductAttribute(product, "currencyId");

    // categories
    for (ShopCategory category : product.directCategoryList()) {
      writeElement("categoryId", category.id().toString());
    }
    writeProductAttribute(product, "picture");
    writeProductAttribute(product, "store");
    writeProductAttribute(product, "pickup");
    writeProductAttribute(product, "delivery");
    writeProductAttribute(product, "typePrefix");
    writeProductAttribute(product, "name");
    writeProductAttribute(product, "vendor");
    writeProductAttribute(product, "vendorCode");
    writeProductAttribute(product, "model");

    // params
    for (ProductAttribute attribute : product.attributeList("param")) {
      streamWriter.writeStartElement(attribute.key());
      for (Map.Entry<String, String> extraAttr :
          attribute.extraInfo().entrySet()) {
        streamWriter.writeAttribute(extraAttr.getKey(), extraAttr.getValue());
      }
      streamWriter.writeCharacters(attribute.value());
      streamWriter.writeEndElement(); //end product attribute
    }

    writeParam("hm_offer_id", product.offer().id());
    writeParam("hm_offer_name", product.offer().name());
    writeParam("hm_original_url", product.url());
    writeParam("hm_name", product.name());

    Tariff tariff = product.tariff();
    if (tariff != null) {
      streamWriter.writeStartElement("param");
      streamWriter.writeAttribute("name", "hm_revenue");
      streamWriter.writeAttribute("unit",
          tariff.cpaPolicy().toString().toLowerCase());
      streamWriter.writeCharacters(tariff.affiliateValue().toString());
      streamWriter.writeEndElement(); //param
    }


    streamWriter.writeEndElement(); //offer
  }


  private void writeProductAttribute(Product product, String name)
      throws XMLStreamException {
    Iterable<ProductAttribute> attributeList = product.attributeList(name);
    for (ProductAttribute attribute : attributeList) {
      writeElement(name, attribute.value());
    }
  }


  private void writeParam(String key, Object value)
      throws XMLStreamException {
    streamWriter.writeStartElement("param");
    streamWriter.writeAttribute("name", key);
    streamWriter.writeCharacters(value.toString());
    streamWriter.writeEndElement();
  }

  private void writeElement(String name, String content)
      throws XMLStreamException {
    streamWriter.writeStartElement(name);
    streamWriter.writeCharacters(content);
    streamWriter.writeEndElement();
  }

  public YmlWriter setTrackerHost(String trackerHost) {
    this.urlMask = trackerHost +
        "/api?method=click&offer_id=%s&aff_id=%s&ulp=%s";
    return this;
  }

  public YmlWriter setUser(User user) {
    this.user = user;
    return this;
  }

  public YmlWriter setSite(Site site) {
    this.site = site;
    return this;
  }
}
