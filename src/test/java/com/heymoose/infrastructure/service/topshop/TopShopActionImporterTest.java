package com.heymoose.infrastructure.service.topshop;

import com.google.common.io.Resources;
import com.heymoose.domain.action.ItemListActionData;
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
    TopShopActionParser converter = new TopShopActionParser();
    List<ItemListActionData> info = converter.parse(
        Resources.newInputStreamSupplier(topShopXml));

    ItemListActionData payment1 = info.get(0);
    ItemListActionData payment2 = info.get(1);
    assertEquals("hm_1", payment1.token());
    assertEquals("hm_2", payment2.token());
    assertEquals(1, payment1.itemList().size());
    assertEquals(2, payment2.itemList().size());
    assertEquals("11", payment1.itemList().get(0).id());
    assertEquals("21", payment2.itemList().get(0).id());
    assertEquals("22", payment2.itemList().get(1).id());
    assertEquals("order_1", payment1.transactionId());
    assertEquals("order_2", payment2.transactionId());
  }

  @Test
  public void parseTopShopItemXml() throws Exception {
    String xml = "<items><item>123</item></items>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopActionParser.XmlTopShopItemList.class);
    TopShopActionParser.XmlTopShopItemList parsedItemList =
        (TopShopActionParser.XmlTopShopItemList)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("123", parsedItemList.itemList.get(0));
  }

  @Test
  public void parseTopShopPaymentWithItem() throws Exception {
    String xml =
        "<payment>" +
          "<key>key</key>" +
          "<order_id>order-id</order_id>" +
          "<items>" +
            "<item>123</item>" +
          "</items>" +
        "</payment>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopActionParser.XmlTopShopPayment.class);
    TopShopActionParser.XmlTopShopPayment parsedPayment =
        (TopShopActionParser.XmlTopShopPayment)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("key", parsedPayment.key);
    assertEquals("order-id", parsedPayment.orderId);
    assertEquals("123", parsedPayment.itemListElement.itemList.get(0));
  }
}
