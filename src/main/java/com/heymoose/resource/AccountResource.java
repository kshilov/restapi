package com.heymoose.resource;

import com.heymoose.domain.accounting.Account;
import com.heymoose.domain.accounting.Accounting;
import com.heymoose.domain.accounting.AccountingEntry;
import com.heymoose.domain.base.Repo;
import com.heymoose.infrastructure.persistence.Transactional;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlAccountingEntries;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.math.BigDecimal;

import static com.heymoose.infrastructure.util.WebAppUtil.checkNotNull;

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

}
