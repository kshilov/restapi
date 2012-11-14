package com.heymoose.domain.cashback;

import com.heymoose.domain.action.OfferAction;
import com.heymoose.domain.user.User;

public class Cashback {

  private String targetId;
  private OfferAction action;
  private User affiliate;

  public String targetId() {
    return targetId;
  }

  public OfferAction action() {
    return action;
  }

  public Cashback setTargetId(String targetId) {
    this.targetId = targetId;
    return this;
  }

  public Cashback setAction(OfferAction action) {
    this.action = action;
    return this;
  }

  public Cashback setAffiliate(User affiliate) {
    this.affiliate = affiliate;
    return this;
  }

  public User affiliate() {
    return this.affiliate;
  }
}
