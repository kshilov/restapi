package com.heymoose.rest.job;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.hibernate.Transactional;
import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.account.Accounts;
import com.heymoose.rest.domain.app.Reservation;
import org.hibernate.Session;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Singleton
public class ReservationCleaner {

  private final Provider<Session> sessionProvider;
  private final int reservationTtl; // hours
  private final Accounts accounts;

  @Inject
  public ReservationCleaner(Provider<Session> sessionProvider, @Named("settings") Properties settings, Accounts accounts) {
    this.sessionProvider = sessionProvider;
    this.accounts = accounts;
    this.reservationTtl = Integer.parseInt(settings.getProperty("reservation-ttl"));
  }

  private Session hiber() {
    return sessionProvider.get();
  }

  @Transactional
  public void deleteExpiredReservations() {
    long now = System.currentTimeMillis();
    Date min = new Date(now - reservationTtl * 60 * 60 * 1000);
    List<Reservation> reservations = hiber()
            .createQuery("from Reservation where done = false and creationTime < :min")
            .setParameter("min", min)
            .list();
    for (Reservation reservation : reservations) {
      Account reservationAccount = reservation.account();
      Account targetAccount = reservation.target().order().account();
      accounts.transfer(reservationAccount, targetAccount, reservationAccount.actual().balance());
      reservation.target().restore();
      reservation.cancel();
    }
  }
}
