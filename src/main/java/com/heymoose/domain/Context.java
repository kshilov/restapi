package com.heymoose.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import org.joda.time.DateTime;

public class Context {
  public final App app;
  public final Integer hour;

  public Context(App app, Integer hour) {
    if (hour == null)
      hour = DateTime.now().minuteOfHour().get();
    checkNotNull(app, hour);
    this.app = app;
    this.hour = hour;
  }
}
