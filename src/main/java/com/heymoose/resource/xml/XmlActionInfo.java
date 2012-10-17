package com.heymoose.resource.xml;

import com.heymoose.infrastructure.service.processing.ProcessableData;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement(name = "action")
public class XmlActionInfo {
  @XmlElement(name = "token")
  public String token;

  @XmlElement(name = "offer-code")
  public String offerCode;

  @XmlElement(name = "transaction-id")
  public String transactionId;

  @XmlElement(name = "price")
  public Double price;

  public ProcessableData toProcessableData() {
    ProcessableData data = new ProcessableData();
    if (price != null)
      data.setPrice(new BigDecimal(price))
          .setToken(token)
          .setTransactionId(transactionId);
    return data;
  }
}
