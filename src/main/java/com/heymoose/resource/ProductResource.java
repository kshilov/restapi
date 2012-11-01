package com.heymoose.resource;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.grant.OfferGrantRepository;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.product.Product;
import com.heymoose.domain.product.ProductAttribute;
import com.heymoose.domain.product.ShopCategory;
import com.heymoose.domain.tariff.Tariff;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.persistence.UserRepositoryHiber;
import com.heymoose.infrastructure.service.Products;
import com.heymoose.infrastructure.util.Cacheable;
import org.hibernate.Transaction;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.joda.time.DateTime;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.heymoose.domain.base.IdEntity.toMap;
import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

@Singleton
@Path("products")
public class ProductResource {

  private final Products products;
  private final OfferGrantRepository grants;
  private final UserRepositoryHiber users;
  private final String urlMask;
  private final Repo repo;

  @Inject
  public ProductResource(Repo repo, Products products,
                         OfferGrantRepository grants,
                         UserRepositoryHiber users,
                         @Named("tracker.host") String trackerHost) {
    this.repo = repo;
    this.products = products;
    this.grants = grants;
    this.users = users;
    this.urlMask = trackerHost +
        "/api?method=click&offer_id=%s&aff_id=%s&ulp=%s";
  }


  @GET
  @Path("feed")
  @Produces("application/xml")
  public StreamingOutput feed(@QueryParam("key") String key,
                     @QueryParam("s") List<Long> offerList,
                     @QueryParam("c") List<Long> categoryList,
                     @QueryParam("q") String queryString,
                     @QueryParam("offset") @DefaultValue("0") int offset,
                     @QueryParam("limit") @DefaultValue("100") int limit) {
    checkNotNull(key);
    // only manual transaction works here because of lazyness
    final Transaction transaction = repo.session().getTransaction();
    try {
      if (!transaction.isActive())  transaction.begin();

      final User user = users.bySecretKey(key);
      if (user == null) throw new WebApplicationException(401);

      final Iterable<Product> productList =
          products.list(user, offerList, categoryList, queryString,
              offset, limit);

      return new StreamingOutput() {
        @Override
        public void write(OutputStream output)
            throws IOException, WebApplicationException {
          try {
            toYml(productList, user,output);
            transaction.commit();
          } catch (XMLStreamException e) {
            transaction.rollback();
            throw new IOException(e);
          } catch (RuntimeException e) {
            transaction.rollback();
            throw e;
          }
        }
      };

    } catch (Throwable e) {
      transaction.rollback();
    }
    throw new WebApplicationException(500);
  }


  @GET
  @Path("categories")
  @Produces("application/xml")
  @Transactional
  @Cacheable(period = "PT1H") // cache for 1 hour
  public String categoryList(@QueryParam("aff_id") Long affId) {
    if (affId == null) throw new WebApplicationException(400);
    Iterable<Offer> grantedProductOffers =
        grants.grantedProductOffers(affId);

    Element result = new Element("result");
    for (Offer offer : grantedProductOffers) {
      Element offerElement = new Element("offer");

      List<ShopCategory> categoryList = products.categoryList(offer.id());
      if (categoryList.isEmpty()) continue;
      Map<Long, ShopCategory> categoryMap =
          toMap(products.categoryList(offer.id()));

      offerElement.addContent(new Element("name").setText(offer.name()));
      offerElement.addContent(toXmlCategories(categoryMap));
      offerElement.setAttribute("id", offer.id().toString());

      result.addContent(offerElement);
    }
    return wrapRoot(result);
  }

  private void toYml(Iterable<Product> productList, User user,
                     OutputStream stream) throws XMLStreamException {
    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(stream);
    streamWriter.writeStartDocument("utf-8", "1.0");
    streamWriter.writeStartElement("yml_catalog");
    streamWriter.writeAttribute("date", DateTime.now().toString());

    streamWriter.writeStartElement("shop");

    writeElement("name", "HeyMoose!", streamWriter);
    writeElement("company", "HeyMoose!", streamWriter);
    writeElement("email", "info@heymoose.com", streamWriter);
    writeElement("url", "http://www.heymoose.com", streamWriter);

    HashMap<Long, ShopCategory> categoryMap = Maps.newHashMap();
    streamWriter.writeStartElement("offers");
    for (Product product : productList) {
      writeProduct(user, product, streamWriter);
      for (ShopCategory category : product.categoryList()) {
        categoryMap.put(category.id(), category);
      }
    }
    streamWriter.writeEndElement(); //offers

    streamWriter.writeStartElement("categories");
    for (ShopCategory category : categoryMap.values()) {
      streamWriter.writeStartElement("category");
      streamWriter.writeAttribute("id", category.id().toString());
      if (category.parentId() != null)
        streamWriter.writeAttribute("parentId", category.parentId().toString());
      streamWriter.writeCharacters(category.name());
      streamWriter.writeEndElement(); //category
    }
    streamWriter.writeEndElement(); //categories

    streamWriter.writeEndElement(); //shop
    streamWriter.writeEndElement(); //yml_catalog
  }

  private void writeProduct(User user, Product product,
                            XMLStreamWriter streamWriter)
      throws XMLStreamException {
    streamWriter.writeStartElement("offer");
    streamWriter.writeAttribute("type", "vendor.model");

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
    writeElement("url", newUrl, streamWriter);

    writeElement("price", product.price().toString(), streamWriter);
    writeProductAttribute(product, "currencyId", streamWriter);

    // categories
    for (ShopCategory category : product.directCategoryList()) {
      writeElement("categoryId", category.id().toString(), streamWriter);
    }
    writeProductAttribute(product, "picture", streamWriter);
    writeProductAttribute(product, "store", streamWriter);
    writeProductAttribute(product, "pickup", streamWriter);
    writeProductAttribute(product, "delivery", streamWriter);
    writeProductAttribute(product, "typePrefix", streamWriter);
    writeProductAttribute(product, "name", streamWriter);
    writeProductAttribute(product, "vendor", streamWriter);
    writeProductAttribute(product, "vendorCode", streamWriter);
    writeProductAttribute(product, "model", streamWriter);

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

    writeParam("hm_offer_id", product.offer().id(), streamWriter);
    writeParam("hm_offer_name", product.offer().name(), streamWriter);
    writeParam("hm_original_url", product.url(), streamWriter);

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

  private void writeProductAttribute(Product product, String name,
                                     XMLStreamWriter streamWriter)
      throws XMLStreamException {
    Iterable<ProductAttribute> attributeList = product.attributeList(name);
    for (ProductAttribute attribute : attributeList) {
      writeElement(name, attribute.value(), streamWriter);
    }
  }

  private String wrapRoot(Element root) {
    return new XMLOutputter().outputString(new Document(root));
  }

  private Element toXmlCategories(Map<Long, ShopCategory> categoryMap) {
    return toXmlCategories(new Element("categories"), categoryMap);
  }

  private Element toXmlCategories(Element categories,
                                  Map<Long, ShopCategory> categoryMap) {
    for (ShopCategory category : categoryMap.values()) {
      Element categoryElement = new Element("category");
      categoryElement.setAttribute("id", category.id().toString());
      if (category.parentId() != null) {
        categoryElement.setAttribute(
            "parentId", category.parentId().toString());
      }
      categoryElement.setText(category.name());
      categories.addContent(categoryElement);
    }
    return categories;
  }

  private void writeParam(String key, Object value,
                          XMLStreamWriter streamWriter)
      throws XMLStreamException {
    streamWriter.writeStartElement("param");
    streamWriter.writeAttribute("name", key);
    streamWriter.writeCharacters(value.toString());
    streamWriter.writeEndElement();
  }

  private void writeElement(String name, String content,
                            XMLStreamWriter streamWriter)
      throws XMLStreamException {
    streamWriter.writeStartElement(name);
    streamWriter.writeCharacters(content);
    streamWriter.writeEndElement();
  }

}
