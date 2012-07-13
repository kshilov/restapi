package com.heymoose.resource;

import com.heymoose.domain.model.User;
import com.heymoose.domain.model.Withdraw;
import com.heymoose.domain.model.accounting.Account;
import com.heymoose.domain.service.Accounting;
import com.heymoose.domain.model.accounting.AccountingEntry;
import com.heymoose.domain.model.base.Repo;
import com.heymoose.infrastructure.hibernate.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlAccountingEntries;
import com.heymoose.resource.xml.XmlWithdraws;
import com.heymoose.util.SqlLoader;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.util.List;

import static com.heymoose.resource.Exceptions.notFound;
import static com.heymoose.util.WebAppUtil.checkNotNull;

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

  @POST
  @Path("{id}/withdraws")
  @Transactional
  public String createWithdraw(@PathParam("id") long accountId) {
    Account account = existing(accountId);
    Withdraw withdraw = accounting.withdraw(account, account.balance());
    return Long.toString(withdraw.id());
  }

  @GET
  @Transactional
  @Path("{id}/withdraws")
  public XmlWithdraws withdrawList(@PathParam("id") long id) {
    Account account = existing(id);
    List<Withdraw> withdraws = accounting.withdraws(account);
    return Mappers.toXmlWithdraws(account.id(), withdraws);
  }

  @GET
  @Transactional
  @Path("aff/{id}/withdraws")
  public XmlWithdraws withdrawListByAff(@PathParam("id") long affId) {
    Account affAccount = existingAffiliateAccount(affId);
    List<Withdraw> withdraws = accounting.withdraws(affAccount);
    return Mappers.toXmlWithdraws(affAccount.id(), withdraws);
  }

  @GET
  @Transactional
  @Path("withdraws")
  public XmlWithdraws allWithdrawList(@QueryParam("offset") @DefaultValue("0") int offset,
                                      @QueryParam("limit") @DefaultValue("20") int limit) {
    String sql = SqlLoader.getSql("withdraw_stats");

    // count without offset and limit
    Query countQuery = repo.session().createSQLQuery(SqlLoader.countSql(sql));
    Long count = SqlLoader.extractLong(countQuery.uniqueResult());

    // query with offset and limit
    Query query = repo.session().createSQLQuery(sql);
    @SuppressWarnings("unchecked")
    List<Object[]> withdraws = query
        .setParameter("offset", offset)
        .setParameter("limit", limit)
        .list();

    // stats on non approved
    String hql = "select count(*), sum(w.amount) from Withdraw w where w.done=:done";
    @SuppressWarnings("unchecked")
    List<Object[]> nonApprovedStat = repo.session().createQuery(hql)
        .setParameter("done", false)
        .list();

    return Mappers.toXmlWithdraws(withdraws, count, nonApprovedStat.get(0)[0], nonApprovedStat.get(0)[1]);
  }

  @PUT
  @Transactional
  @Path("withdraws/{id}")
  public void approveWithdraw(@PathParam("id") long id) {
    Withdraw withdraw = existingWithdraw(id);
    accounting.approveWithdraw(withdraw);
  }

  @DELETE
  @Transactional
  @Path("withdraws/{id}")
  public void deleteWithdraw(@PathParam("id") long id,
                             @FormParam("comment") String comment) {
    checkNotNull(comment);
    Withdraw withdraw = existingWithdraw(id);
    accounting.deleteWithdraw(withdraw, comment);
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
  
  private Withdraw existingWithdraw(long id) {
    Withdraw withdraw = repo.get(Withdraw.class, id);
    if (withdraw == null)
      throw notFound();
    return withdraw;
  }

  private Account existing(long id) {
    Account account = repo.get(Account.class, id);
    if (account == null)
      throw notFound();
    return account;
  }
}
