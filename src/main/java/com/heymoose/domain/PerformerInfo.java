package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkArgument;

public class PerformerInfo {

  public final Performer performer;
  public final String city;

  public PerformerInfo(Performer performer, String city) {
    checkArgument(performer != null);
    this.performer = performer;
    this.city = city;
  }
}
