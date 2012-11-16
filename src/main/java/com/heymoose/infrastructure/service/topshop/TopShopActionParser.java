package com.heymoose.infrastructure.service.topshop;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.ItemListActionData;
import com.heymoose.infrastructure.service.action.ActionDataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public final class TopShopActionParser
    implements ActionDataParser<ItemListActionData> {

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
    public String orderId;
    @XmlElement
    public String cart;
    @XmlElement
    public String status;
    @XmlElement(name = "items")
    public XmlTopShopItemList itemListElement;

    @Override
    public String toString() {
      return Objects.toStringHelper(XmlTopShopPayment.class)
          .add("orderId", orderId)
          .add("key", key)
          .toString();
    }
  }

  @XmlRootElement(name = "items")
  public static class XmlTopShopItemList {

    @XmlElement(name = "item")
    public List<String> itemList = Lists.newArrayList();
  }

  private static final Logger log =
      LoggerFactory.getLogger(TopShopActionParser.class);
  private static final char STATUS_CREATED = '1';
  private static final char STATUS_COMPLETE = '2';
  private static final char STATUS_CANCELED = '3';

  /**
   * @param inputSupplier input supplier with top shop xml
   * @return map token - price
   */
  @SuppressWarnings("unchecked")
  public List<ItemListActionData> parse(InputSupplier<InputStream> inputSupplier) {
    InputStream input = null;
    ImmutableList.Builder<ItemListActionData> dataBuilder =
        ImmutableList.builder();
    try {
      input = inputSupplier.getInput();
      BufferedInputStream bufferedInput =
          new BufferedInputStream(input);
      JAXBContext context = JAXBContext.newInstance(XmlTopShopPayments.class);
      XmlTopShopPayments paymentList = (XmlTopShopPayments)
          context.createUnmarshaller().unmarshal(bufferedInput);
      for (XmlTopShopPayment payment : paymentList.paymentList) {
        try {
          dataBuilder.add(parsePayment(payment));
        } catch (Exception e) {
          log.warn("Exception during parsing: {}", payment);
          log.error("Skipping payment with exception", e);
        }
      }
      return dataBuilder.build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(input);
    }
  }

  private ItemListActionData parsePayment(XmlTopShopPayment payment)
      throws Exception {
    Map<String, String> requestParamMap = parseParamMap(
        new URL(payment.key));
    String token = requestParamMap.get("_hm_token");
    if (token == null) {
      log.warn("Token param not found in url {} for order: {}. Skipping",
          payment.key, payment.orderId);
      throw new IllegalArgumentException("No token in url: " + payment.key);
    }
    ItemListActionData paymentData = new ItemListActionData();
    paymentData.setToken(token);
    paymentData.setTransactionId(payment.orderId);
    switch (payment.status.charAt(0)) {
      case STATUS_CREATED:
        paymentData.setStatus(ActionStatus.CREATED);
        break;
      case STATUS_COMPLETE:
        paymentData.setStatus(ActionStatus.COMPLETE);
        break;
      case STATUS_CANCELED:
        paymentData.setStatus(ActionStatus.CANCELED);
    }
    for (String item : payment.itemListElement.itemList) {
      paymentData.addItem(item);
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
