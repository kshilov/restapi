package com.heymoose.rest.domain.account;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
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
  public void transfer(AccountOwner from, AccountOwner to, BigDecimal amount) {
    String desc = String.format("Transfering %s from %s to %s", amount, from, to);
    AccountTx tx1 = from.subtractFromBalance(amount, desc);
    AccountTx tx2 = to.addToBalance(amount, desc);
    hiber().save(tx1);
    hiber().save(tx2);
  }
}
