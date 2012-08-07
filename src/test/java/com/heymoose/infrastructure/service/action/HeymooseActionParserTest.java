package com.heymoose.infrastructure.service.action;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionData;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class HeymooseActionParserTest {

  @Test
  public void parseSingleActionNoItems() throws Exception {
    final String xml =
        "<actions>" +
          "<action>" +
            "<token>token-value</token>" +
            "<transaction>transaction-value</transaction>" +
            "<status>1</status>" +
          "</action>" +
        "</actions>";
    HeymooseActionParser parser = new HeymooseActionParser();
    List<ActionData> result = parser.parse(input(xml));

    assertEquals(1, result.size());
    ActionData resultAction = result.get(0);
    assertEquals("token-value", resultAction.token());
    assertEquals("transaction-value", resultAction.transactionId());
    assertEquals(ActionData.Status.COMPLETE, resultAction.status());
    assertEquals(0, resultAction.itemList().size());
  }

  @Test
  public void parseSingleActionWithItem() throws Exception {
    String xml =
        "<actions>" +
            "<action>" +
              "<items>" +
                "<item>" +
                  "<id>123</id>" +
                  "<price>0.01</price>" +
                  "<quantity>2</quantity>" +
                "</item>" +
              "</items>" +
            "</action>" +
        "</actions>";
    HeymooseActionParser parser = new HeymooseActionParser();
    List<ActionData> resultList = parser.parse(input(xml));
    ActionData result = resultList.get(0);

    assertEquals(1, result.itemList().size());
    ActionData.Item resultItem = result.itemList().get(0);
    assertEquals("123", resultItem.id());
    assertEquals(new BigDecimal("0.01"), resultItem.price());
    assertEquals(2, resultItem.quantity());
  }

  private InputSupplier<InputStream> input(final String input) {
    return new InputSupplier<InputStream>() {
      @Override
      public InputStream getInput() throws IOException {
        return new ByteArrayInputStream(input.getBytes());
      }
    };
  }
}
