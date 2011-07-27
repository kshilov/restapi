package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.question.BaseQuestion;
import com.heymoose.rest.domain.question.Reservable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "reservation")
public class Reservation extends IdEntity {

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time")
  private Date creationTime;

  @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id")
  private Reservable target;

  @Basic
  private boolean done;

  private Reservation() {}

  public Reservation(Reservable reservable) {
    this.creationTime = new Date();
    this.account = new Account();
    this.target = reservable;
    this.target.addReservation(this);
  }

  public Account account() {
    return account;
  }

  public Reservable target() {
    return target;
  }

  public Date creationTime() {
    return creationTime;
  }

  public void cancel() {
    done = true;
  }

  public boolean done() {
    return done;
  }
}
