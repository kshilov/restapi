package com.heymoose.rest.domain.account;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import org.hibernate.Hibernate;
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

  private void lockStaleObject(Object obj) {
    hiber().flush();
    hiber().buildLockRequest(LockOptions.UPGRADE).lock(obj);
    hiber().refresh(obj);
  }

  @Transactional
  public void transfer(Account from, Account to, BigDecimal amount) {
    if (from.equals(to))
      throw new IllegalArgumentException("Accounts must not be same");

    if (from.id() > to.id()) {
      lockStaleObject(from);
      lockStaleObject(to);
    } else {
      lockStaleObject(to);
      lockStaleObject(from);
    }

    String desc = String.format("Transfering %s from %s to %s", amount, from.id(), to.id());
    AccountTx tx1 = from.subtractFromBalance(amount, desc);
    AccountTx tx2 = to.addToBalance(amount, desc);
    hiber().save(tx1);
    hiber().save(tx2);
  }
}
