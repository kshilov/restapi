package com.heymoose.resource.xml;

import com.google.common.collect.Lists;
import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.statistics.OfferStat;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "actions")
public final class XmlOfferActions {

  protected XmlOfferActions() { }

  public XmlOfferActions(List<OfferAction> actionList, Long count) {
    for (OfferAction action : actionList) {
      this.actionList.add(new XmlOfferAction(action));
    }
    this.count = count;
  }

  @XmlRootElement(name = "action")
  protected static final class XmlOfferAction {
    @XmlElement
    public Date date;
    @XmlElement(name = "transaction_id")
    public String transactionId;
    @XmlElement(name = "partner_id")
    public Long partnerId;
    @XmlElement(name = "not-confirmed-revenue")
    public BigDecimal notConfirmedRevenue;
    @XmlElement(name = "confirmed-revenue")
    public BigDecimal confirmedRevenue;
    @XmlElement(name = "canceled-revenue")
    public BigDecimal canceledRevenue;
    @XmlElement
    public String state;

    protected XmlOfferAction() { }

    public XmlOfferAction(OfferAction action) {
      OfferStat stat = action.stat();
      this.date = action.creationTime().toDate();
      this.state = action.state().toString();
      this.canceledRevenue = stat.canceledRevenue().add(stat.canceledFee());
      this.confirmedRevenue = stat.confirmedRevenue().add(stat.confirmedFee());
      this.notConfirmedRevenue = stat.notConfirmedRevenue().add(stat.notConfirmedFee());
      this.partnerId = action.affiliate().id();
      this.transactionId = action.transactionId();
    }
  }

  @XmlElement(name ="action")
  public List<XmlOfferAction> actionList = Lists.newArrayList();

  @XmlAttribute
  public Long count;
}
