package com.heymoose.infrastructure.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public final class TopShopActionImporterTest {

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

    }


    /**
     * @param inputSupplier input supplier with top shop xml
     * @return map token - price
     */
    @SuppressWarnings("unchecked")
    public Map<String, BigDecimal> convert(InputSupplier<InputStream> inputSupplier) {
      InputStream input = null;
      ImmutableMap.Builder<String, BigDecimal> resultBuilder =
          ImmutableMap.builder();
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
          resultBuilder.put(token, new BigDecimal(payment.cart));
        }
        return resultBuilder.build();
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
    Map<String, BigDecimal> info = converter.convert(
        Resources.newInputStreamSupplier(topShopXml));

    assertEquals(new BigDecimal("100.500"), info.get("heymoose_token_100.500"));
    assertEquals(new BigDecimal("100.501"), info.get("heymoose_token_100.501"));
  }
}
