package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.ItemListActionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

public final class HeymooseItemListParser
    implements ActionDataParser<ItemListActionData> {

  @XmlRootElement(name = "actions")
  protected static class XmlActions {

    @XmlElement(name = "action")
    public List<XmlAction> actionList = Lists.newArrayList();

  }

  @XmlRootElement(name = "action")
  protected static class XmlAction {

    @XmlElement
    public String token;

    @XmlElement
    public String transaction;

    @XmlElement
    public int status;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<XmlItem> itemList = Lists.newArrayList();
  }

  @XmlRootElement(name = "item")
  protected static class XmlItem {

    @XmlElement
    public String id;

    @XmlElement
    public BigDecimal price;

    @XmlElement
    public BigDecimal quantity = BigDecimal.ONE;
  }

  private static final Logger log =
      LoggerFactory.getLogger(HeymooseItemListParser.class);

  @Override
  public List<ItemListActionData> parse(InputSupplier<InputStream> inputSupplier) {
    InputStream input = null;
    ImmutableList.Builder<ItemListActionData> dataBuilder =
        ImmutableList.builder();
    try {
      input = inputSupplier.getInput();
      BufferedInputStream bufferedInput =
          new BufferedInputStream(input);
      JAXBContext context = JAXBContext.newInstance(XmlActions.class);
      XmlActions xmlActionList = (XmlActions)
          context.createUnmarshaller().unmarshal(bufferedInput);
      for (XmlAction xmlAction : xmlActionList.actionList) {
        ItemListActionData data = new ItemListActionData();
        data.setToken(xmlAction.token)
            .setTransactionId(xmlAction.transaction)
            .setStatus(ActionStatus.values()[xmlAction.status]);
        for (XmlItem xmlItem : xmlAction.itemList) {
          data.addItem(xmlItem.id, xmlItem.price, xmlItem.quantity.intValue());
        }
        dataBuilder.add(data);
      }
      return dataBuilder.build();
    } catch (Exception e) {
      log.error("Could not parse heymoose actions xml", e);
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(input);
    }
  }

}
