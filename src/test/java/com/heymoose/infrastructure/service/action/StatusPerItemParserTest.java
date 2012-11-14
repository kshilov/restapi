package com.heymoose.infrastructure.service.action;

import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.ItemWithStatus;
import com.heymoose.domain.action.StatusPerItemActionData;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public final class StatusPerItemParserTest {

  @Test
  public void parseSingleItem() throws Exception {
    String transactionId = "transactionId";
    String token = "012345678901234567890123456789012";
    String itemId = "itemId";
    String price = "10.01";

    String xml = ""
        + "<actions>"
            + "<action>"
              + "<transaction>" + transactionId + "</transaction>"
              + "<token>" + token + "</token>"
              + "<items>"
                + "<item>"
                  + "<id>" + itemId + "</id>"
                  + "<price>" + price + "</price>"
                  + "<status>1</status>"
                + "</item>"
              + "</items>"
            + "</action>"
        + "</actions>";
    List<StatusPerItemActionData> data =
        new StatusPerItemParser().parse(supplier(xml));

    assertEquals(1, data.size());
    StatusPerItemActionData action = data.get(0);
    assertEquals(transactionId, action.transactionId());
    assertEquals(token, action.token());
    assertEquals(1, action.itemList().size());
    ItemWithStatus item = action.itemList().get(0);
    assertEquals(itemId, item.id());
    assertEquals(new BigDecimal(price), item.price());
    assertEquals(ActionStatus.COMPLETE, item.status());

  }

  private InputSupplier<InputStream> supplier(final String string) {
    return new InputSupplier<InputStream>() {
      @Override
      public InputStream getInput() throws IOException {
        return new ByteArrayInputStream(string.getBytes());
      }
    };
  }
}
