package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.hibernate.LockOptions;
import org.hibernate.Session;

@Singleton
public class Accounts {

  private final Provider<Session> sessionProvider;

  @Inject
  public Accounts(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  public void lock(Account account) {
    hiber().flush();
    hiber().buildLockRequest(LockOptions.UPGRADE).lock(account);
  }

  public void lock(Account a1, Account a2) {
    if (a1.id() > a2.id()) {
      lock(a1);
      lock(a2);
    } else {
      lock(a2);
      lock(a1);
    }
  }
  
  public Account getAndLock(long accountId) {
    return (Account) hiber().get(Account.class, accountId, LockOptions.UPGRADE);
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

  public Account get(long accountId) {
    return (Account) hiber().get(Account.class, accountId);
  }
  
  public void transfer(Account from, Account to, BigDecimal amount) {
    checkArgument(amount.signum() > 0);
    String desc = String.format("Transferring %s from account:%d to account:%d", Double.toString(amount.doubleValue()), from.id(), to.id());
    subtractFromBalance(from, amount, desc, TxType.TRANSFER);
    addToBalance(to, amount, desc, TxType.TRANSFER);
  }

  public AccountTx subtractFromBalance(Account account, BigDecimal amount, String desc, TxType type) {
    AccountTx lastTx = lastTxOf(account);
    if (lastTx == null)
      throw new IllegalStateException("No enough money");
    AccountTx newTx = lastTx.subtract(amount, desc, account.allowNegativeBalance(), type);
    hiber().save(newTx);
    return newTx;
  }
  
  public AccountTx addToBalance(Account account, BigDecimal amount, String desc, TxType type) {
    AccountTx lastTx = lastTxOf(account);
    if (lastTx != null
        && type == TxType.ACTION_APPROVED
        && lastTx.type() == TxType.ACTION_APPROVED) {
      lastTx.addInPlace(amount, desc);
    }
    AccountTx newTx = (lastTx == null) ?
        new AccountTx(account, amount, type) : lastTx.add(amount, desc, type);
    hiber().save(newTx);
    return newTx;
  }

  public AccountTx lastTxOf(Account account) {
    return (AccountTx) hiber()
        .createQuery("from AccountTx where account = :account order by version desc")
        .setParameter("account", account)
        .setMaxResults(1)
        .uniqueResult();
  }

  public void withdraw(Account account, TxType txType) {
    Withdraw lastWithdraw = (Withdraw) hiber()
        .createQuery("from Withdraw where account = :account order by timestamp desc")
        .setParameter("account", account)
        .setMaxResults(1)
        .uniqueResult();
    List<AccountTx> transactions;
    if (lastWithdraw == null) {
      transactions = hiber()
          .createQuery("from AccountTx where account = :account and type = :type")
          .setParameter("account", account)
          .setParameter("type", txType)
          .list();
    } else {
      int lastVersion = lastWithdraw.lastTx().version();
      transactions = hiber()
          .createQuery("from AccountTx where account = :account and type = :type and version > :version")
          .setParameter("account", account)
          .setParameter("type", txType)
          .setParameter("version", lastVersion)
          .list();
    }
    if (transactions.isEmpty())
      return;
    BigDecimal amount = new BigDecimal(0);
    for (AccountTx tx : transactions) {
      checkState(tx.diff().signum() == 1);
      amount = amount.add(tx.diff());
    }
    transactions = newArrayList(transactions);
    Collections.sort(transactions);
    AccountTx lastTx = transactions.get(transactions.size() - 1);
    subtractFromBalance(account, amount, "Withdraw", TxType.WITHDRAW);
    Withdraw withdraw = new Withdraw(account, lastTx);
    hiber().save(withdraw);
  }
}
