package com.heymoose.infrastructure.service.topshop;

import com.google.common.io.Resources;
import com.heymoose.domain.action.ActionData;
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
    List<ActionData> info = converter.convert(
        Resources.newInputStreamSupplier(topShopXml));

    ActionData payment1 = info.get(0);
    ActionData payment2 = info.get(1);
    assertEquals("hm_1", payment1.token());
    assertEquals("hm_2", payment2.token());
    assertEquals(1, payment1.itemList().size());
    assertEquals(2, payment2.itemList().size());
    assertEquals(11L, payment1.itemList().get(0).id());
    assertEquals(21L, payment2.itemList().get(0).id());
    assertEquals(22L, payment2.itemList().get(1).id());
    assertEquals("order_1", payment1.transactionId());
    assertEquals("order_2", payment2.transactionId());
  }

  @Test
  public void parseTopShopItemXml() throws Exception {
    String xml = "<items><item>123</item></items>";
    StringReader reader = new StringReader(xml);
    JAXBContext context = JAXBContext.newInstance(
        TopShopXmlConverter.XmlTopShopItemList.class);
    TopShopXmlConverter.XmlTopShopItemList parsedItemList =
        (TopShopXmlConverter.XmlTopShopItemList)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals(123, (long) parsedItemList.itemList.get(0));
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
        TopShopXmlConverter.XmlTopShopPayment.class);
    TopShopXmlConverter.XmlTopShopPayment parsedPayment =
        (TopShopXmlConverter.XmlTopShopPayment)
            context.createUnmarshaller().unmarshal(reader);
    assertEquals("key", parsedPayment.key);
    assertEquals("order-id", parsedPayment.orderId);
    assertEquals(123L, (long) parsedPayment.itemListElement.itemList.get(0));
  }
}
