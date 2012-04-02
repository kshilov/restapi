package com.heymoose.domain.accounting;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Predicate;
import com.heymoose.domain.Withdraw;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.hibernate.Transactional;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@Singleton
public class Accounting {

  private final Repo repo;

  @Inject
  public Accounting(Repo repo) {
    this.repo = repo;
  }

  @Transactional
  public Account createAccount(BigDecimal balance) {
    Account account = new Account();
    AccountingEntry entry = createEntry(account, balance);
    repo.put(entry);
    return account;
  }

  @Transactional
  public void lock(Account a1, Account a2) {
    if (a1.id() > a2.id()) {
      repo.lock(a1);
      repo.lock(a2);
    } else {
      repo.lock(a2);
      repo.lock(a1);
    }
  }

  @Transactional
  public void transferMoney(Account src, Account dst, BigDecimal amount, AccountingTransactionType type, Long event, String descr) {
    AccountingEntry srcEntry = createEntry(src, amount);
    AccountingEntry dstEntry = createEntry(dst, amount);
    createTransaction(srcEntry, dstEntry, type, event, descr);
  }

  @Transactional
  private void createTransaction(AccountingEntry srcEntry, AccountingEntry dstEntry, AccountingTransactionType type, Long event, String descr) {
    AccountingTransaction transaction = new AccountingTransaction(srcEntry, dstEntry, type,  event, descr);
    repo.put(transaction);
  }

  @Transactional
  public AccountingEntry createEntry(Account account, BigDecimal amount) {
    return new AccountingEntry(account, amount);
  }

  @Transactional
  private AccountingEntry lastEntry(Account account) {
    DetachedCriteria criteria = DetachedCriteria
        .forClass(AccountingEntry.class)
        .add(Restrictions.eq("account", account))
        .addOrder(Order.desc("id"));
    return (AccountingEntry) repo.getExecutableCriteria(criteria).setMaxResults(1).uniqueResult();
  }

  @Transactional
  public AccountingEntry amendLastEntry(Account account, BigDecimal amount, Predicate<AccountingEntry> amendCondition) {
    AccountingEntry lastEntry = lastEntry(account);
    if (amendCondition.apply(lastEntry))
      return lastEntry.amend(amount);
    else
      return createEntry(account, amount);
  }

  public Withdraw withdraw(Account account, BigDecimal amount) {
    createEntry(account, amount.negate());
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
    createEntry(withdraw.account(), withdraw.amount());
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
