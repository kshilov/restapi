package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;
import com.heymoose.util.Pair;
import java.math.BigDecimal;
import org.hibernate.LockOptions;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import sun.management.counter.AbstractCounter;

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
    hiber().refresh(account);
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
    from.subtractFromBalance(amount, desc);
    to.addToBalance(amount, desc);
  }
}
