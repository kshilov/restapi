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

  private static final class XmlAffiliateBasic {
    @XmlAttribute
    public Long id;
    @XmlElement
    public String email;

    public XmlAffiliateBasic(Map<String, Object> action) {
      this.id = extractLong(action.get("affiliate_id"));
      this.email = extractString(action.get("affiliate_email"));
    }

    protected XmlAffiliateBasic() { }
  }

  private static final class XmlOfferBasic {
    @XmlAttribute
    public Long id;
    @XmlElement
    public String code;
    @XmlElement
    public String title;

    public XmlOfferBasic(Map<String, Object> action) {
      this.code = extractString(action.get("offer_code"));
      this.title = extractString(action.get("offer_title"));
      this.id = extractLong(action.get("offer_id"));
    }

    protected XmlOfferBasic() { }
  }

  @XmlRootElement(name = "action")
  private static final class XmlOfferAction {
    @XmlAttribute
    public Long id;
    @XmlElement(name = "creation-time")
    public Date creationTime;
    @XmlElement(name = "last-change-time")
    public Date lastChangeTime;
    @XmlElement(name = "transaction-id")
    public String transactionId;
    @XmlElement(name = "affiliate")
    public XmlAffiliateBasic affiliate;
    @XmlElement(name = "offer")
    public XmlOfferBasic offer;
    @XmlElement(name = "amount")
    public BigDecimal amount;
    @XmlElement
    public String state;

    protected XmlOfferAction() { }

    public XmlOfferAction(Map<String, Object> action) {
      this.id = extractLong(action.get("id"));
      this.creationTime = extractDateTime(action.get("creation_time")).toDate();
      this.lastChangeTime = extractDateTime(action.get("last_change_time")).toDate();
      Integer stateId = extractInteger(action.get("state"));
      this.state = OfferActionState.values()[stateId].toString();
      this.amount = scaledDecimal(action.get("amount"));
      this.affiliate = new XmlAffiliateBasic(action);
      this.offer = new XmlOfferBasic(action);
      this.transactionId = extractString(action.get("transaction_id"));
    }
  }

  @XmlElement(name ="action")
  public List<XmlOfferAction> actionList = Lists.newArrayList();

  @XmlAttribute
  public Long count;
}
