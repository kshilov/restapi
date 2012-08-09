package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.domain.action.OfferActionState;
import com.heymoose.infrastructure.util.QueryResult;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.heymoose.infrastructure.util.SqlLoader.*;

@XmlRootElement(name = "actions")
public final class XmlOfferActions {

  protected XmlOfferActions() { }

  public XmlOfferActions(QueryResult actionList, Long count) {
    for (Map<String, Object> action : actionList) {
      this.actionList.add(new XmlOfferAction(action));
    }
    this.count = count;
  }

  @XmlRootElement(name = "action")
  protected static final class XmlOfferAction {
    @XmlAttribute
    public Long id;
    @XmlElement(name = "creation-time")
    public Date creationTime;
    @XmlElement(name = "transaction-id")
    public String transactionId;
    @XmlElement(name = "affiliate")
    public XmlUser affiliate;
    @XmlElement(name = "offer")
    public XmlBaseOffer offer;
    @XmlElement(name = "amount")
    public BigDecimal amount;
    @XmlElement
    public String state;

    protected XmlOfferAction() { }

    public XmlOfferAction(Map<String, Object> action) {
      this.id = extractLong(action.get("id"));
      this.creationTime = extractDateTime(action.get("creation_time")).toDate();
      Integer stateId = extractInteger(action.get("state"));
      this.state = OfferActionState.values()[stateId].toString();
      this.amount = scaledDecimal(action.get("amount"));
      this.affiliate = null;
      this.offer = null;
      this.transactionId = action.get("transaction_id").toString();
    }
  }

  @XmlElement(name ="action")
  public List<XmlOfferAction> actionList = Lists.newArrayList();

  @XmlAttribute
  public Long count;
}
