package com.heymoose.infrastructure.service.action;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.StatusPerItemActionData;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.joda.time.DateTime;

import java.io.InputStream;
import java.util.List;

public class StatusPerItemParser
    implements ActionDataParser<StatusPerItemActionData> {

  @Override
  public List<StatusPerItemActionData> parse(InputSupplier<InputStream> input) {
    ImmutableList.Builder<StatusPerItemActionData> result =
        ImmutableList.builder();
    SAXBuilder builder = new SAXBuilder();
    Document document;
    try {
      document = builder.build(input.getInput());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    for (Element xmlAction: document.getRootElement().getChildren("action")) {
      StatusPerItemActionData actionData = new StatusPerItemActionData();
      String createdString = xmlAction.getChildText("created");
      if (!Strings.isNullOrEmpty(createdString)) {
        actionData.setCreationTime(new DateTime(createdString));
      }
      String changedString = xmlAction.getChildText("changed");
      if (!Strings.isNullOrEmpty(changedString)) {
        actionData.setLastChangeTime(new DateTime(changedString));
      }
      actionData.setTransactionId(xmlAction.getChildText("transaction"))
          .setToken(xmlAction.getChildText("token"));
      for (Element xmlItem : xmlAction.getChild("items").getChildren("item")) {
        String id = xmlItem.getChildText("id");
        StatusPerItemActionData.ItemWithStatus item =
            new StatusPerItemActionData.ItemWithStatus(id);
        item.setStatus(xmlItem.getChildText("status"))
            .setPrice(xmlItem.getChildText("price"))
            .setQuantity(xmlItem.getChildText("quantity"));
        actionData.addItem(item);
      }
      result.add(actionData);
    }
    return result.build();
  }
}
