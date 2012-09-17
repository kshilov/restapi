package com.heymoose.resource;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.base.Repo;
import com.heymoose.domain.offer.Offer;
import com.heymoose.domain.user.User;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.infrastructure.util.DataFilter;
import com.heymoose.infrastructure.util.OrderingDirection;
import com.heymoose.infrastructure.util.Pair;
import com.heymoose.infrastructure.util.QueryResult;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlAccountingEntries;
import com.heymoose.resource.xml.XmlQueryResult;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.heymoose.infrastructure.util.WebAppUtil.*;
import static com.heymoose.resource.Exceptions.notFound;

@Path("account")
@Singleton
public class AccountResource {

  private final Repo repo;
  private final Accounting accounting;

  @Inject
  public AccountResource(Repo repo, Accounting accounting) {
    this.repo = repo;
    this.accounting = accounting;
  }

  @POST
  @Path("transfer")
  @Transactional
  public void transfer(@FormParam("from") Long fromAccountId,
                       @FormParam("to") Long toAccountId,
                       @FormParam("amount") Double _amount) {
    checkNotNull(fromAccountId, toAccountId, _amount);
    BigDecimal amount = new BigDecimal(_amount);
    Account src = repo.get(Account.class, fromAccountId);
    Account dst = repo.get(Account.class, toAccountId);
    accounting.transferMoney(src, dst, amount, null, null);
  }

  @GET
  @Path("{id}/entries")
  @Transactional
  public XmlAccountingEntries entryList(@PathParam("id") Long accountId,
                                        @QueryParam("offset") @DefaultValue("0") int offset,
                                        @QueryParam("limit") @DefaultValue("20") int limit) {
    DetachedCriteria criteria = DetachedCriteria.forClass(AccountingEntry.class)
        .add(Restrictions.eq("account.id", accountId))
        .addOrder(Order.desc("creationTime"));
    Iterable<AccountingEntry> entries = repo.pageByCriteria(criteria, offset, limit);

    criteria = DetachedCriteria.forClass(AccountingEntry.class)
        .add(Restrictions.eq("account.id", accountId));
    Long count = repo.countByCriteria(criteria);

    return Mappers.toXmlAccountingEntries(entries, count);
  }


  @PUT
  @Transactional
  @Path("withdraw")
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
      accounting.offerToAffiliate(
          offer,
          userIdList.get(i), amountList.get(i),
          new DateTime(from) ,new DateTime(to));
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
                                Accounting.DebtOrdering ord,
                                @QueryParam("direction") @DefaultValue("DESC")
                                OrderingDirection dir,
                                @QueryParam("offset") int offset,
                                @QueryParam("limit") @DefaultValue("20")
                                int limit) {
    checkNotNull(offerId);
    DateTime dateFrom = new DateTime(from);
    DateTime dateTo = new DateTime(to);
    DataFilter<Accounting.DebtOrdering> filter = DataFilter.newInstance();
    filter.setTo(dateTo)
        .setFrom(dateFrom)
        .setOrdering(ord)
        .setDirection(dir)
        .setOffset(offset)
        .setLimit(limit);

    Pair<QueryResult, Long> result = accounting.debtGroupedByAffiliate(
        offerId, filter);
    Map<String, Object> sum =
        accounting.sumDebtForOffer(offerId, dateFrom, dateTo);
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
                            Accounting.DebtOrdering ord,
                            @QueryParam("direction") @DefaultValue("DESC")
                            OrderingDirection dir,
                            @QueryParam("offset") int offset,
                            @QueryParam("limit") @DefaultValue("20")
                            int limit) {
    checkNotNull(affId);
    DateTime dateFrom = new DateTime(from);
    DateTime dateTo = new DateTime(to);
    DataFilter<Accounting.DebtOrdering> filter = DataFilter.newInstance();
    filter.setTo(dateTo)
        .setFrom(dateFrom)
        .setOrdering(ord)
        .setDirection(dir)
        .setOffset(offset)
        .setLimit(limit);

    Pair<QueryResult, Long> result = accounting.debtGroupedByOffer(
        affId, filter);
    Map<String, Object> sum =
        accounting.sumDebtForAffiliate(affId, dateFrom, dateTo);
    return new XmlQueryResult(result.fst)
        .setElement("debt")
        .setRoot("debts")
        .addRootAttribute("count", result.snd)
        .addRootAttributesFrom(sum)
        .toString();
  }


  private Account existingAffiliateAccount(long id) {
    User user = repo.get(User.class, id);
    if (user == null)
      throw notFound();
    Account account = user.affiliateAccount();
    if (account == null)
      throw notFound();
    return account;
  }
  
  private Account existing(long id) {
    Account account = repo.get(Account.class, id);
    if (account == null)
      throw notFound();
    return account;
  }

  private Offer existingOffer(long id) {
    Offer offer = repo.get(Offer.class, id);
    if (offer == null)
      throw new WebApplicationException(404);
    return offer;

  }
}
