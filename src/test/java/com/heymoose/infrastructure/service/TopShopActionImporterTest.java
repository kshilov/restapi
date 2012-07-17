package com.heymoose.infrastructure.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public final class TopShopActionImporterTest {

  public static class TopShopPaymentData {
    private String heymooseToken;
    private Map<String, BigDecimal> itemPriceMap = Maps.newHashMap();
  }

  public static final class TopShopXmlConverter {

    @XmlRootElement(name = "payment_list")
    public static class XmlTopShopPayments {

      @XmlElement(name = "payment")
      public List<XmlTopShopPayment> paymentList = Lists.newArrayList();
    }

    @XmlRootElement(name = "payment")
    public static class XmlTopShopPayment {
      @XmlElement
      public String key;
      @XmlElement(name = "order_id")
      public Long orderId;
      @XmlElement
      public String cart;
      @XmlElement
      public int status;
      @XmlElement(name = "item_list")
      public XmlTopShopItemList itemListElement;
    }

    @XmlRootElement(name = "item_list")
    public static class XmlTopShopItemList {

      @XmlElement(name = "item")
      public List<XmlTopShopItem> itemList = Lists.newArrayList();
    }

    @XmlRootElement(name = "item")
    public static class XmlTopShopItem {

      @XmlElement
      public String code;
      @XmlElement
      public String price;
    }


    /**
     * @param inputSupplier input supplier with top shop xml
     * @return map token - price
     */
    @SuppressWarnings("unchecked")
    public List<TopShopPaymentData> convert(InputSupplier<InputStream> inputSupplier) {
      InputStream input = null;
      ImmutableList.Builder<TopShopPaymentData> dataBuilder =
          ImmutableList.builder();
      try {
        input = inputSupplier.getInput();
        BufferedInputStream bufferedInput =
            new BufferedInputStream(input);
        JAXBContext context = JAXBContext.newInstance(XmlTopShopPayments.class);
        XmlTopShopPayments paymentList = (XmlTopShopPayments)
            context.createUnmarshaller().unmarshal(bufferedInput);
        for (XmlTopShopPayment payment : paymentList.paymentList) {
          Map<String, String> requestParamMap = parseParamMap(
              new URL(payment.key));
          String token = requestParamMap.get("_hm_token");
          if (token == null)
            continue;
          TopShopPaymentData paymentData = new TopShopPaymentData();
          paymentData.heymooseToken = token;
          for (XmlTopShopItem item : payment.itemListElement.itemList) {
            paymentData.itemPriceMap.put(item.code, new BigDecimal(item.price));
          }
          dataBuilder.add(paymentData);
        }
        return dataBuilder.build();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        try {
          input.close();
        } catch (Exception e) {

        }
      }
    }

    private Map<String, String> parseParamMap(URL url) {
      String queryString = url.getQuery();
      ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
      String[] keyValArray = queryString.split("&");
      for (String keyVal : keyValArray) {
        String[] keyValSplited = keyVal.split("=");
        builder.put(keyValSplited[0], keyValSplited[1]);
      }
      return builder.build();
    }
  }
  

  @Test
  public void parsesTopShopXmlCorrectly() throws Exception {
    URL topShopXml = getClass().getClassLoader()
        .getResource("topshop/example.xml");
    TopShopXmlConverter converter = new TopShopXmlConverter();
    List<TopShopPaymentData> info = converter.convert(
        Resources.newInputStreamSupplier(topShopXml));

    TopShopPaymentData payment1 = info.get(0);
    TopShopPaymentData payment2 = info.get(1);
    assertEquals("hm_1", payment1.heymooseToken);
    assertEquals("hm_2", payment2.heymooseToken);
    assertEquals(new BigDecimal("100.500"), payment1.itemPriceMap.get("hm_1_item_1"));
    assertEquals(new BigDecimal("100.501"), payment2.itemPriceMap.get("hm_2_item_1"));
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
