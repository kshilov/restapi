package com.heymoose.domain.accounting;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@Singleton
public class Accounting {

  private final Repo repo;

  @Inject
  public Accounting(Repo repo) {
    this.repo = repo;
  }

  public Account createAccount(BigDecimal balance) {
    Account account = new Account();
    AccountingEntry entry = new AccountingEntry(account, balance);
    repo.put(entry);
    return account;
  }

  public void lock(Account a1, Account a2) {
    if (a1.id() > a2.id()) {
      repo.lock(a1);
      repo.lock(a2);
    } else {
      repo.lock(a2);
      repo.lock(a1);
    }
  }

  public void transferMoney(Account src, Account dst, BigDecimal amount, AccountingEvent event, Long sourceId, String descr) {
    checkArgument(amount.signum() == 1);
    AccountingEntry srcEntry = new AccountingEntry(src, amount.negate(), event, sourceId, descr);
    AccountingEntry dstEntry = new AccountingEntry(dst, amount, event, sourceId, descr);
    createTransaction(srcEntry, dstEntry);
  }

  public void transferMoney(Account src, AccountingEntry dstEntry, BigDecimal amount, AccountingEvent event, Long sourceId, String desc) {
    checkArgument(amount.signum() == 1);
    dstEntry.amend(amount);
    AccountingEntry srcEntry = new AccountingEntry(src, amount.negate(), event, sourceId, desc);
    srcEntry.setTransaction(dstEntry.transaction());
  }

  public void cancel(AccountingTransaction transaction) {
    List<AccountingEntry> entries = repo.allByHQL(
        AccountingEntry.class,
        "from AccountingEntry where transaction = ?",
        transaction
    );
    AccountingTransaction reverseTx = new AccountingTransaction();
    for (AccountingEntry entry : entries) {
      AccountingEntry reverseEntry = new AccountingEntry(entry.account(), entry.amount().negate());
      reverseEntry.setTransaction(reverseTx);
      repo.put(reverseEntry);
    }
  }

  private void createTransaction(AccountingEntry srcEntry, AccountingEntry dstEntry) {
    AccountingTransaction transaction = new AccountingTransaction();
    repo.put(transaction);
    srcEntry.setTransaction(transaction);
    dstEntry.setTransaction(transaction);
  }

  public AccountingEntry getLastEntry(Account account) {
    DetachedCriteria criteria = DetachedCriteria.forClass(AccountingEntry.class);
    criteria.add(Restrictions.eq("account", account));
    criteria.addOrder(Order.desc("id"));
    return (AccountingEntry) repo.getExecutableCriteria(criteria).setMaxResults(1).uniqueResult();
  }

  public Withdraw withdraw(Account account, BigDecimal amount) {
    new AccountingEntry(account, amount.negate());
    Withdraw withdraw = new Withdraw(account, amount);
    repo.put(withdraw);
    return withdraw;
  }

  public List<Withdraw> withdraws(Account account) {
    return repo.allByHQL(Withdraw.class, "from Withdraw where account = ? order by timestamp desc", account);
  }

  public Withdraw withdrawOfAccount(Account account, long withdrawId) {
    return repo.byHQL(Withdraw.class, "from Withdraw where account = ? and id = ?", account, withdrawId);
  }

  public void deleteWithdraw(Withdraw withdraw, String comment) {
    checkArgument(!isBlank(comment));
    new AccountingEntry(withdraw.account(), withdraw.amount());
    repo.remove(withdraw);
  }

  public Account getAndLock(Long accountId) {
    Account account = repo.get(Account.class, accountId);
    repo.lock(account);
    return account;
  }

  public Pair<Account, Account> getAndLock(long accountId1, long accountId2) {
    if (accountId1 > accountId2) {
      return Pair.of(getAndLock(accountId1), getAndLock(accountId2));
    } else {
      Account account2 = getAndLock(accountId2);
      Account account1 = getAndLock(accountId1);
      return Pair.of(account1, account2);
    }
  }
}
