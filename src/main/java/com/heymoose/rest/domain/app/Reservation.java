package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.question.QuestionBase;

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

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id")
  private Account account;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id")
  private QuestionBase question;

  @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private UserProfile user;

  @Basic
  private boolean done;

  private Reservation() {}

  public Reservation(QuestionBase reservable, UserProfile user) {
    this.creationTime = new Date();
    this.account = new Account();
    this.question = reservable;
    this.user = user;
    this.question.addReservation(this);
  }

  public Account account() {
    return account;
  }

  public QuestionBase target() {
    return question;
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

  public UserProfile user() {
    return user;
  }
}
