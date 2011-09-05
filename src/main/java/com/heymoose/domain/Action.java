package com.heymoose.domain;

import com.heymoose.domain.base.IdEntity;

import java.util.Date;

public class Action extends IdEntity {

  public String title;
  public String body;
  public ActionType type;
  public byte[] image;
  public Date creationTime;

  public static enum ActionType {
    URL_ACTION
  }
}
