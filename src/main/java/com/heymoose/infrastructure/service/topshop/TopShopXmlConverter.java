package com.heymoose.infrastructure.service.topshop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.InputSupplier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public final class TopShopXmlConverter {

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
        paymentData.setToken(token);
        for (XmlTopShopItem item : payment.itemListElement.itemList) {
          paymentData.addItem(item.code, item.price);
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
