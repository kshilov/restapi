package com.heymoose.resource;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;

import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlAccountingEntries;
import com.heymoose.util.Pair;
import com.sun.grizzly.tcp.Response;

import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Singleton;
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
    Pair<Account, Account> pair = accounting.getAndLock(fromAccountId, toAccountId);
    accounting.transferMoney(pair.fst, pair.snd, amount, null, null);
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
  
  private Account existing(long id) {
    Account account = repo.get(Account.class, id);
    if (account == null)
      throw notFound();
    return account;
  }
}
