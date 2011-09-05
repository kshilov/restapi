package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Set;

public class User extends IdEntity {
  public Set<Order> orders;
  public Set<App> apps;

  public String email;
  public String passwordHash;
  public String nickname;
}
