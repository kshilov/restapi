package com.heymoose.resource;

import com.google.inject.Inject;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.service.Debts;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.resource.xml.XmlQueryResult;
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

  private final Repo repo;
  private final Debts debts;

  @Inject
  public WithdrawalResource(Repo repo, Debts debts) {
    this.repo = repo;
    this.debts = debts;
  }


  @PUT
  @Transactional
  public Response makeWithdraw(@FormParam("offer_id") Long offerId,
                               @FormParam("user_id") List<Long> userIdList,
                               @FormParam("amount") List<BigDecimal> amountList,
                               @FormParam("from") @DefaultValue("0") Long from,
                               @FormParam("to") Long to) {
    checkCondition(userIdList.size() == amountList.size());
    checkNotNull(offerId);
    Offer offer = existingOffer(offerId);
    for (int i = 0; i < userIdList.size(); i++) {
      checkNotNull(userIdList.get(i), amountList.get(i));
      try {
        debts.payOffToAffiliate(
            offer,
            userIdList.get(i), amountList.get(i),
            new DateTime(from) ,new DateTime(to));
      } catch (IllegalArgumentException e) {
        return Response.status(409).build();
      }
    }
    return Response.ok().build();
  }


  @GET
  @Path("debt/by_affiliate")
  @Produces("application/xml")
  @Transactional
  public String debtByAffiliate(@QueryParam("offer_id") Long offerId,
                                @QueryParam("from") @DefaultValue("0") Long from,
                                @QueryParam("to") Long to,
                                @QueryParam("ordering") @DefaultValue("DEBT")
                                Debts.Ordering ord,
                                @QueryParam("direction") @DefaultValue("DESC")
                                OrderingDirection dir,
                                @QueryParam("offset") int offset,
                                @QueryParam("limit") @DefaultValue("20")
                                int limit) {
    checkNotNull(offerId);
    DateTime dateFrom = new DateTime(from);
    DateTime dateTo = new DateTime(to);
    DataFilter<Debts.Ordering> filter = DataFilter.newInstance();
    filter.setTo(dateTo)
        .setFrom(dateFrom)
        .setOrdering(ord)
        .setDirection(dir)
        .setOffset(offset)
        .setLimit(limit);

    Pair<QueryResult, Long> result = debts.groupedByAffiliate(
        offerId, filter);
    Map<String, Object> sum = debts.sumForOffer(offerId, dateFrom, dateTo);
    return new XmlQueryResult(result.fst)
        .setElement("debt")
        .setRoot("debts")
        .addRootAttribute("count", result.snd)
        .addRootAttributesFrom(sum)
        .toString();
  }


  @GET
  @Path("debt/by_offer")
  @Produces("application/xml")
  @Transactional
  public String debtByOffer(@QueryParam("aff_id") Long affId,
                            @QueryParam("from") @DefaultValue("0") Long from,
                            @QueryParam("to") Long to,
                            @QueryParam("ordering") @DefaultValue("DEBT")
                            Debts.Ordering ord,
                            @QueryParam("direction") @DefaultValue("DESC")
                            OrderingDirection dir,
                            @QueryParam("offset") int offset,
                            @QueryParam("limit") @DefaultValue("20")
                            int limit) {
    checkNotNull(affId);
    DateTime dateFrom = new DateTime(from);
    DateTime dateTo = new DateTime(to);
    DataFilter<Debts.Ordering> filter = DataFilter.newInstance();
    filter.setTo(dateTo)
        .setFrom(dateFrom)
        .setOrdering(ord)
        .setDirection(dir)
        .setOffset(offset)
        .setLimit(limit);

    Pair<QueryResult, Long> result = debts.groupedByOffer(affId, filter);
    Map<String, Object> sum = debts.sumForAffiliate(affId, dateFrom, dateTo);
    return new XmlQueryResult(result.fst)
        .setElement("debt")
        .setRoot("debts")
        .addRootAttribute("count", result.snd)
        .addRootAttributesFrom(sum)
        .toString();
  }


  private Offer existingOffer(long id) {
    Offer offer = repo.get(Offer.class, id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;
  }
}
