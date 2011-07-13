package com.heymoose.rest.domain.app;

import com.heymoose.rest.domain.account.AccountOwner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "reservation")
public class Reservation extends AccountOwner<Reservation> {

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "creation_time")
  public Date creationTime;

  
}
