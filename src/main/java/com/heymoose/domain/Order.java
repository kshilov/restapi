package com.heymoose.domain;

import java.util.Date;

public class Order extends IdEntity {
  public Account account;
  public Offer offer;
  public Date creationTime;
}
