package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.account.Account;
import com.heymoose.rest.domain.base.IdEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
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

  public Reservation() {
    this.creationTime = new Date();
    this.account = new Account();
  }

  public Account account() {
    return account;
  }

  public Date creationTime() {
    return creationTime;
  }
}
