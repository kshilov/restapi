package com.heymoose.rest.domain.account;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;

import java.math.BigDecimal;

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

  @Transactional
  public void transfer(Account from, Account to, BigDecimal amount) {
    if (from.equals(to))
      throw new IllegalArgumentException("Accounts must not be same");

    Account lock1 = from.id() > to.id() ? from : to;
    Account lock2 = from.id() > to.id() ? to : from;
    hiber().buildLockRequest(LockOptions.UPGRADE).lock(lock1);
    hiber().buildLockRequest(LockOptions.UPGRADE).lock(lock2);

    String desc = String.format("Transfering %s from %s to %s", amount, from, to);
    AccountTx tx1 = from.subtractFromBalance(amount, desc);
    AccountTx tx2 = to.addToBalance(amount, desc);
    hiber().save(tx1);
    hiber().save(tx2);
  }
}
