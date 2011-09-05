package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Date;

public class Performing extends IdEntity {
  public Performer performer;
  public Action action;
  public Date creationTime = new Date();
  public AccountTx reservation;
}
