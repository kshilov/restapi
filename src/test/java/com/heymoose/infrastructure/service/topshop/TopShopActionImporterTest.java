package com.heymoose.infrastructure.service.topshop;

import com.google.common.io.Resources;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class TopShopActionImporterTest {

  @Test
  public void parsesTopShopXmlCorrectly() throws Exception {
    URL topShopXml = getClass().getClassLoader()
        .getResource("topshop/example.xml");
    TopShopXmlConverter converter = new TopShopXmlConverter();
    List<TopShopPaymentData> info = converter.convert(
        Resources.newInputStreamSupplier(topShopXml));

    TopShopPaymentData payment1 = info.get(0);
    TopShopPaymentData payment2 = info.get(1);
    assertEquals("hm_1", payment1.token());
    assertEquals("hm_2", payment2.token());
    assertEquals(new BigDecimal("100.500"), payment1.price("hm_1_item_1"));
    assertEquals(new BigDecimal("100.501"), payment2.price("hm_2_item_1"));
  }

  @Test
  public void parseTopShopItemXml() throws Exception {
    String xml = "<item><code>123</code><price>00.01</price></item>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopXmlConverter.XmlTopShopItem.class);
    TopShopXmlConverter.XmlTopShopItem parsedItem =
        (TopShopXmlConverter.XmlTopShopItem)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("123", parsedItem.code);
    assertEquals("00.01", parsedItem.price);
  }

  @Test
  public void parseTopShopPaymentWithItem() throws Exception {
    String xml =
        "<payment>" +
          "<key>key</key>" +
          "<item_list>" +
            "<item><code>123</code><price>00.01</price></item>" +
          "</item_list>" +
        "</payment>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopXmlConverter.XmlTopShopPayment.class);
    TopShopXmlConverter.XmlTopShopPayment parsedPayment =
        (TopShopXmlConverter.XmlTopShopPayment)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("key", parsedPayment.key);
    assertEquals("123", parsedPayment.itemListElement.itemList.get(0).code);
    assertEquals("00.01", parsedPayment.itemListElement.itemList.get(0).price);
  }
}
