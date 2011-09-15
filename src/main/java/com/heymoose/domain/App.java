package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Date;

public class App extends IdEntity {
  public Platform platform;
  public Date creationTime;
  public String secret;
  public User user;
  public boolean deleted;
}
