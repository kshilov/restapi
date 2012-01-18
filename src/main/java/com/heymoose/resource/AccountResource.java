package com.heymoose.resource;

import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.domain.Account;
import com.heymoose.domain.AccountTx;
import com.heymoose.domain.Accounts;
import com.heymoose.hibernate.Transactional;
import static com.heymoose.resource.Exceptions.notFound;
import com.heymoose.resource.xml.Mappers;
import com.heymoose.resource.xml.XmlTransactions;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
    Account account = accounts.get(accountId);
    if (account == null)
      throw notFound();
    List<AccountTx> transactions = newArrayList(account.transactions());
    Collections.sort(transactions, new Comparator<AccountTx>() {
      @Override
      public int compare(AccountTx o1, AccountTx o2) {
        return o2.version().compareTo(o1.version());
      }
    });
    List<AccountTx> page = newArrayList();
    int cnt = 0;
    for(int i = offset; i < transactions.size() && cnt < limit; cnt++, i++)
      page.add(transactions.get(i));
    return Mappers.toXmlTransactions(page, transactions.size());
  }
}
