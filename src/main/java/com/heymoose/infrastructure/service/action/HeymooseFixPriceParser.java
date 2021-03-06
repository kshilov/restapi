package com.heymoose.infrastructure.service.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.heymoose.domain.action.ActionStatus;
import com.heymoose.domain.action.FixPriceActionData;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

public final class HeymooseFixPriceParser
    implements ActionDataParser<FixPriceActionData> {

  private static final Logger log = LoggerFactory.getLogger(
      HeymooseFixPriceParser.class);

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

    @XmlElement(name = "offer")
    public String offerCode;

    @XmlElement(name = "date")
    public String date;

  }

  public List<FixPriceActionData> parse(InputSupplier<InputStream> inputSupplier) {
    InputStream input = null;
    ImmutableList.Builder<FixPriceActionData> dataBuilder =
        ImmutableList.builder();
    try {
      input = inputSupplier.getInput();
      BufferedInputStream bufferedInput =
          new BufferedInputStream(input);
      JAXBContext context = JAXBContext.newInstance(XmlActions.class);
      XmlActions xmlActionList = (XmlActions)
          context.createUnmarshaller().unmarshal(bufferedInput);
      for (XmlAction xmlAction : xmlActionList.actionList) {
        FixPriceActionData data = new FixPriceActionData()
            .setOfferCode(xmlAction.offerCode);
        data.setStatus(ActionStatus.values()[xmlAction.status])
            .setToken(xmlAction.token)
            .setTransactionId(xmlAction.transaction);
        if (xmlAction.date != null) {
            data.setLastChangeTime(DateTimeFormat
                .forPattern("YYYY-MM-dd HH:mm:SS")
                .parseDateTime(xmlAction.date));
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
