package com.heymoose.rest.domain.question;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
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
    Reservation reservation = (Reservation) hiber()
            .createQuery("from Reservation where question = :question and app = :app")
            .setParameter("question", answer.question())
            .setParameter("app", answer.user().app())
            .uniqueResult();
    
  }
}
