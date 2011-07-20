package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.base.IdEntity;
import com.heymoose.rest.domain.order.Order;
import com.heymoose.rest.domain.question.BaseQuestion;

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
import javax.persistence.UniqueConstraint;
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
  @JoinColumn(name = "question_id")
  private BaseQuestion question;

  private Reservation() {}

  public Reservation(BaseQuestion question) {
    this.creationTime = new Date();
    this.account = new Account();
    this.question = question;
    this.question.addReservation(this);
  }

  public Account account() {
    return account;
  }

  public BaseQuestion question() {
    return question;
  }

  public Date creationTime() {
    return creationTime;
  }
}
