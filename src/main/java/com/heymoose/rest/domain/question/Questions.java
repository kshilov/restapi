package com.heymoose.rest.domain.question;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.account.Accounts;
import com.heymoose.rest.domain.app.App;
import com.heymoose.rest.domain.app.Reservation;
import com.heymoose.rest.domain.order.Order;
import org.hibernate.Session;

@Singleton
public class Questions {

  private final Accounts accounts;

  private final Provider<Session> sessionProvider;

  @Inject
  public Questions(Accounts accounts, Provider<Session> sessionProvider) {
    this.accounts = accounts;
    this.sessionProvider = sessionProvider;
  }

  protected Session hiber() {
    return sessionProvider.get();
  }

  @Transactional
  public void reserve(BaseQuestion question) {
    Reservation reservation = new Reservation(question);
    hiber().save(reservation);
    Order order = question.order();
    accounts.transfer(order.account(), reservation.account(), order.costPerAnswer());
    question.ask();
  }
}
