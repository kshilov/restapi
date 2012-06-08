package com.heymoose.job;

import org.joda.time.DateTime;

public interface Job {
  public void run(DateTime plannedStartTime) throws Exception;
}
