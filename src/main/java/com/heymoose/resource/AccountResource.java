package com.heymoose.resource;

import com.heymoose.domain.Account;
import com.heymoose.domain.Accounts;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlCount;
import com.heymoose.resource.xml.XmlTransactions;
import com.heymoose.util.Pair;
import static com.heymoose.util.WebAppUtil.checkNotNull;
import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("account")
@Singleton
public class AccountResource {
  
  private final Accounts accounts;

  @Inject
  public AccountResource(Accounts accounts) {
    this.accounts = accounts;
  }

  @GET
  @Path("{accountId}/transactions")
  @Transactional
  public XmlTransactions transactions(@PathParam("accountId") long accountId, 
                                      @QueryParam("offset") @DefaultValue("0") int offset,
                                      @QueryParam("limit") @DefaultValue("20") int limit) {
    Account account = existing(accountId);
    return Mappers.toXmlTransactions(accounts.transactions(offset, limit, account));
  }
  
  @GET
  @Path("{accountId}/transactions/count")
  @Transactional
  public XmlCount transactionsCount(@PathParam("accountId") long accountId) {
    Account account = existing(accountId);
    return Mappers.toXmlCount(accounts.transactionsCount(account));
  }

  @POST
  @Path("transfer")
  @Transactional
  public void transfer(@FormParam("from") Long fromAccountId, 
                       @FormParam("to") Long toAccountId, 
                       @FormParam("amount") Double _amount) {
    checkNotNull(fromAccountId, toAccountId, _amount);
    BigDecimal amount = new BigDecimal(_amount);
    Pair<Account, Account> pair = accounts.getAndLock(fromAccountId,toAccountId);
    accounts.transfer(pair.fst, pair.snd, amount);
  }
  
  private Account existing(long id) {
    Account account = accounts.get(id);
    if (account == null)
      throw notFound();
    return account;
  }
}
