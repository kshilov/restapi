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
    @XmlAttribute
    public Long id;
    @XmlElement
    public Date date;
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

    public XmlOfferAction(OfferAction action) {
      OfferStat stat = action.stat();
      this.id = action.id();
      this.date = action.creationTime().toDate();
      this.state = action.state().toString();
      this.amount = stat
          .canceledRevenue().add(stat.canceledFee())
          .add(stat.confirmedRevenue()).add(stat.confirmedFee())
          .add(stat.notConfirmedRevenue()).add(stat.notConfirmedFee());
      this.affiliate = Mappers.toXmlUser(action.affiliate());
      this.offer = new XmlBaseOffer(action.offer());
      this.transactionId = action.transactionId();
    }
  }

  @XmlElement(name ="action")
  public List<XmlOfferAction> actionList = Lists.newArrayList();

  @XmlAttribute
  public Long count;
}
