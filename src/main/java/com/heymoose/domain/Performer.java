package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Date;

public class Performer extends IdEntity {
  public String extId;
  public App app;
  public Date creationTime;
  public Performer inviter;
}
