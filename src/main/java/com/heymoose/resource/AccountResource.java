package com.heymoose.resource;

import com.heymoose.domain.Withdraw;
import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.accounting.AccountingEvent;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlAccountingEntries;
import com.heymoose.resource.xml.XmlWithdraws;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import java.util.List;
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
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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
  public XmlAccountingEntries entriesList(@PathParam("id") Long accountId,
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
  public XmlWithdraws withdrawsList(@PathParam("id") long id) {
    Account account = existing(id);
    List<Withdraw> withdraws = accounting.withdraws(account);
    return Mappers.toXmlWithdraws(account.id(), withdraws);
  }

  @PUT
  @Transactional
  @Path("{id}/withdraws/{withdrawId}")
  public void approveWithdraw(@PathParam("id") long id, @PathParam("withdrawId") long withdrawId) {
    Account account = existing(id);
    Withdraw withdraw = existingWithdraw(account, withdrawId);
    AccountingEntry entry = existingAccountingEntry(AccountingEvent.WITHDRAW, withdrawId);
    accounting.applyEntry(entry);
    withdraw.approve();
  }

  @DELETE
  @Transactional
  @Path("{id}/withdraws/{withdrawId}")
  public void deleteDeveloperWithdraw(@PathParam("id") long id,
                                      @PathParam("withdrawId") long withdrawId,
                                      @FormParam("comment") String comment) {
    checkNotNull(comment);
    Account account = existing(id);
    Withdraw withdraw = existingWithdraw(account, withdrawId);
    accounting.deleteWithdraw(withdraw, comment);
  }

  private Withdraw existingWithdraw(Account account, long id) {
    Withdraw withdraw = accounting.withdrawOfAccount(account, id);
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

  private AccountingEntry existingAccountingEntry(AccountingEvent event, long sourceId) {
    DetachedCriteria criteria = DetachedCriteria.forClass(AccountingEntry.class)
        .add(Restrictions.eq("source_id", sourceId))
        .add(Restrictions.eq("event", event));
    AccountingEntry entry = repo.byCriteria(criteria);
    if (entry == null)
      throw notFound();
    return entry;
  }
}
