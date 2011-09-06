package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Date;

public class Action extends IdEntity {
  public Performer performer;
  public Offer offer;
  public Date creationTime;
  public AccountTx reservation;
}
