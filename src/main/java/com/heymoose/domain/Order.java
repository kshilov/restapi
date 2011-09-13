package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.math.BigDecimal;
import java.util.Date;

public class Order extends IdEntity {
  public Account account;
  public Offer offer;
  public Date creationTime;
  public BigDecimal cpa;
  public User user;
  public boolean approved;
}
