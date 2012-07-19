package com.heymoose.infrastructure.service.topshop;

import com.google.common.io.Resources;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import java.io.StringReader;
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
    assertEquals(1, payment1.items().size());
    assertEquals(2, payment2.items().size());
    assertEquals("hm_1_item_1", payment1.items().get(0));
    assertEquals("hm_2_item_1", payment2.items().get(0));
    assertEquals("hm_2_item_2", payment2.items().get(1));
    assertEquals("order_1", payment1.transactionId());
    assertEquals("order_2", payment2.transactionId());
  }

  @Test
  public void parseTopShopItemXml() throws Exception {
    String xml = "<item_list><item>123</item></item_list>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopXmlConverter.XmlTopShopItemList.class);
    TopShopXmlConverter.XmlTopShopItemList parsedItemList =
        (TopShopXmlConverter.XmlTopShopItemList)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("123", parsedItemList.itemList.get(0));
  }

  @Test
  public void parseTopShopPaymentWithItem() throws Exception {
    String xml =
        "<payment>" +
          "<key>key</key>" +
          "<order_id>order-id</order_id>" +
          "<item_list>" +
            "<item>123</item>" +
          "</item_list>" +
        "</payment>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopXmlConverter.XmlTopShopPayment.class);
    TopShopXmlConverter.XmlTopShopPayment parsedPayment =
        (TopShopXmlConverter.XmlTopShopPayment)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("key", parsedPayment.key);
    assertEquals("order-id", parsedPayment.orderId);
    assertEquals("123", parsedPayment.itemListElement.itemList.get(0));
  }
}
