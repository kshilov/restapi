package com.heymoose.rest.domain.question;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.account.Accounts;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.Reservation;
import org.hibernate.Session;

@Singleton
public class Answers {

  private final Provider<Session> sessionProvider;
  private final Accounts accounts;

  @Inject
  public Answers(Provider<Session> sessionProvider, Accounts accounts) {
    this.sessionProvider = sessionProvider;
    this.accounts = accounts;
  }

  protected Session hiber() {
    return sessionProvider.get();
  }

  @Transactional
  public void acceptAnswer(BaseAnswer answer) {
    if (answer.question().hasForm())
      throw new IllegalArgumentException();
    Reservation reservation = (Reservation) hiber()
            .createQuery("from Reservation where target = :question")
            .setParameter("question", answer.question())
            .uniqueResult();
    if (reservation.done())
      throw new IllegalStateException();
    Account reservationAccount = reservation.account();
    Account appAccount = answer.user().app().account();
    accounts.transfer(reservationAccount, appAccount, reservationAccount.actual().balance());
    reservation.cancel();
  }
}
