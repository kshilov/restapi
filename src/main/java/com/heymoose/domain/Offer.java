package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Date;

public class Offer extends IdEntity {
  
  public String title;
  public String body;
  public Type type;
  public byte[] image;
  public Date creationTime;
  public Order order;

  public static enum Type {
    URL
  }
}
