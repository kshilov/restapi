package com.heymoose.domain;

import org.hibernate.LockOptions;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

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
}
