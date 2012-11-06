package com.heymoose.resource;

import com.google.inject.Inject;
import com.heymoose.domain.accounting.Withdrawal;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Debts;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.resource.xml.XmlQueryResult;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.joda.time.DateTime;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.heymoose.infrastructure.util.WebAppUtil.*;

@Path("withdrawals")
public class WithdrawalResource {

  private static final XMLOutputter OUTPUTTER = new XMLOutputter();

  private final Repo repo;
  private final Debts debts;

  @Inject
  public WithdrawalResource(Repo repo, Debts debts) {
    this.repo = repo;
    this.debts = debts;
  }
  
  @GET
  @Produces("application/xml")
  @Transactional
  public String listOrdered(@QueryParam("aff_id") Long affId,
                            @QueryParam("offset") int offset,
                            @QueryParam("limit") @DefaultValue("20") int limit) {
    DataFilter<Debts.Ordering> filter = DataFilter.newInstance();
    filter.setOffset(offset).setLimit(limit);
    Pair<QueryResult, Long> result = debts.orderedByUser(affId, filter);
    return new XmlQueryResult(result.fst)
      .setRoot("debts")
      .setElement("debt")
      .addRootAttribute("count", result.snd)
      .toString();
  }
  
  @GET
  @Path("sum")
  @Produces("application/xml")
  @Transactional
  public String sumOrdered(@QueryParam("aff_id") Long affId) {
    return toXmlSum(debts.sumOrderedByUser(affId));
  }

  @GET
  @Produces("application/xml")
  @Path("by_offer")
  @Transactional
  public String listOrdered(@QueryParam("from") @DefaultValue("0") Long from,
                            @QueryParam("to") Long to,
                            @QueryParam("offset") int offset,
                            @QueryParam("limit") @DefaultValue("20") int limit) {
    DataFilter<Debts.Ordering> filter = DataFilter.newInstance();
    filter.setOffset(offset).setLimit(limit).setFrom(from).setTo(to);
    Pair<QueryResult, Long> result = debts.orderedByOffer(filter);
    return new XmlQueryResult(result.fst)
        .setRoot("debts")
        .setElement("debt")
        .addRootAttribute("count", result.snd)
        .toString();
  }
  @PUT
  @Transactional
  public Response makeWithdraw(@FormParam("offer_id") Long offerId,
                               @FormParam("user_id") List<Long> userIdList,
                               @FormParam("basis") List<Withdrawal.Basis> basisList,
                               @FormParam("amount") List<BigDecimal> amountList,
                               @FormParam("date_kind") Debts.DateKind dateKind,
                               @FormParam("from") @DefaultValue("0") Long from,
                               @FormParam("to") Long to) {
    checkCondition(userIdList.size() == amountList.size());
    checkNotNull(offerId);
    Offer offer = existingOffer(offerId);
    for (int i = 0; i < userIdList.size(); i++) {
      checkNotNull(userIdList.get(i), amountList.get(i));
      try {
        debts.payOffToAffiliate(
            offer, userIdList.get(i), basisList.get(i), amountList.get(i),
            dateKind, new DateTime(from) ,new DateTime(to));
      } catch (IllegalArgumentException e) {
        return Response.status(409).build();
      }
    }
    return Response.ok().build();
  }

  @PUT
  @Transactional
  @Path("order")
  public Response orderWithdrawal(@FormParam("aff_id") Long affId) {
    checkNotNull(affId);
    debts.orderWithdrawal(affId);
    return Response.ok().build();
  }


  @GET
  @Path("debt")
  @Produces("application/xml")
  @Transactional
  public String debt(@QueryParam("offer_id") Long offerId,
                     @QueryParam("aff_id") Long affId,
                     @QueryParam("date_kind") @DefaultValue("CREATION")
                     Debts.DateKind dateKind,
                     @QueryParam("from") @DefaultValue("0") Long from,
                     @QueryParam("to") Long to,
                     @QueryParam("ordering") @DefaultValue("DEBT")
                     Debts.Ordering ord,
                     @QueryParam("direction") @DefaultValue("DESC")
                     OrderingDirection dir,
                     @QueryParam("offset") int offset,
                     @QueryParam("limit") @DefaultValue("20")
                     int limit) {
    DateTime dateFrom = new DateTime(from);
    DateTime dateTo = new DateTime(to);
    DataFilter<Debts.Ordering> filter = DataFilter.newInstance();
    filter.setTo(dateTo)
        .setFrom(dateFrom)
        .setOrdering(ord)
        .setDirection(dir)
        .setOffset(offset)
        .setLimit(limit);

    Pair<QueryResult, Long> result =
        debts.debtInfo(offerId, affId, dateKind, filter);
    return new XmlQueryResult(result.fst)
        .setElement("debt")
        .setRoot("debts")
        .addRootAttribute("count", result.snd)
        .toString();
  }

  @GET
  @Path("debt/sum")
  @Transactional
  @Produces("application/xml")
  public String sumDebt(@QueryParam("aff_id") Long affId,
                        @QueryParam("offer_id") Long offerId,
                        @QueryParam("date_kind") @DefaultValue("CREATION")
                        Debts.DateKind dateKind,
                        @QueryParam("from") @DefaultValue("0") Long from,
                        @QueryParam("to") Long to) {
    return toXmlSum(debts.sumDebt(affId, offerId,
            dateKind, new DateTime(from), new DateTime(to)));
  }
  
  private Offer existingOffer(long id) {
    Offer offer = repo.get(Offer.class, id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }

  @GET
  @Path("masspayment")
  @Transactional
  @Produces("application/xml")
  public String massPayment(@QueryParam("from") @DefaultValue("0") Long from,
                            @QueryParam("to") Long to) {
    DataFilter<Debts.PaymentOrdering> filter = DataFilter.newInstance();
    filter.setFrom(from)
        .setTo(to)
        .setOrdering(Debts.PaymentOrdering.AMOUNT)
        .setDirection(OrderingDirection.DESC);
    return massPaymentXml(debts.payments(Debts.PayMethod.AUTO, filter));
  }

  @GET
  @Path("payments")
  @Transactional
  @Produces("application/xml")
  public String payments(@QueryParam("pay_method") Debts.PayMethod payMethod,
                         @QueryParam("from") @DefaultValue("0") Long from,
                         @QueryParam("to") Long to,
                         @QueryParam("offset") int offset,
                         @QueryParam("limit") @DefaultValue("20") int limit,
                         @QueryParam("ordering") @DefaultValue("AMOUNT")
                         Debts.PaymentOrdering ordering,
                         @QueryParam("direction") @DefaultValue("DESC")
                         OrderingDirection direction) {
    DataFilter<Debts.PaymentOrdering> filter = DataFilter.newInstance();
    filter.setFrom(from)
        .setTo(to)
        .setOffset(offset)
        .setLimit(limit)
        .setOrdering(ordering)
        .setDirection(direction);
    return paymentsXml(debts.payments(payMethod, filter));
  }



  private String paymentsXml(Pair<QueryResult, Long> queryResult) {
    Element rootElement = new Element("payments");
    rootElement.setAttribute("count", queryResult.snd.toString());
    for (Map<String, Object> record : queryResult.fst) {
      Element affiliateElement = new Element("affiliate")
          .setAttribute("id", record.get("affiliate_id").toString())
          .addContent(element("email", record.get("affiliate_email")))
          .addContent(element("wmr", record.get("affiliate_wmr")));


      Element paymentElement = new Element("payment")
          .addContent(affiliateElement)
          .addContent(element("basis", record.get("basis")))
          .addContent(element("amount", record.get("amount")))
          .addContent(element("pay-method", record.get("pay_method")));

      if (record.get("offer_id") != null) {
        Element offerElement = new Element("offer")
            .setAttribute("id", record.get("offer_id").toString())
            .addContent(element("name", record.get("offer_name")));
        paymentElement.addContent(offerElement);
      }

      rootElement.addContent(paymentElement);
    }
    return OUTPUTTER.outputString(rootElement);
  }

  private String massPaymentXml(Pair<QueryResult, Long> result) {
    Element rootElement = new Element("payments")
        .setNamespace(Namespace.getNamespace("http://tempuri.org/ds.xsd"));
    for (Map<String, Object> payment : result.fst) {
      Withdrawal.Basis basis = Withdrawal.Basis.valueOf(
          payment.get("basis").toString());
      String description = null;
      switch (basis) {
        case AFFILIATE_REVENUE:
          description = String.format(
              "Выплата вознаграждения партнёру %s. Оффер \"%s\".",
              payment.get("affiliate_email"), payment.get("offer_name"));
          break;
        case MLM:
          description = String.format(
              "Выплата вознаграждения партнёру %s. Реферальная программа.",
              payment.get("affiliate_email"));
          break;
      }
      String id = payment.get("affiliate_id").toString();
      Element paymentElement = new Element("payment")
          .addContent(element("Destination", payment.get("affiliate_wmr")))
          .addContent(element("Amount", payment.get("amount")))
          .addContent(element("Description", description))
          .addContent(element("Id", id));

      rootElement.addContent(paymentElement);
    }
    return OUTPUTTER.outputString(rootElement);
  }


  private String toXmlSum(QueryResult result) {
    Element root = new Element("debt");
    for (Map.Entry<String, Object> entry: result.get(0).entrySet()) {
      Element attribute = new Element(entry.getKey())
          .setText(entry.getValue().toString());
      root.addContent(attribute);
    }
    return OUTPUTTER.outputString(new Document(root));
  }

  private Element element(String name, Object content) {
    if (content == null)
      return new Element(name);
    return new Element(name).setText(content.toString());
  }
}
