package com.heymoose.job;

import org.joda.time.DateTime;

public class StartTimeCalculator {
  
  private StartTimeCalculator() {}

  public static DateTime calcStartTime(int runAtHours, int runAtMinutes) {
    DateTime startTime = DateTime.now();
    startTime = startTime.withHourOfDay(runAtHours);
    startTime = startTime.withMinuteOfHour(runAtMinutes);
    startTime = startTime.withSecondOfMinute(0);
    startTime = startTime.withMillisOfSecond(0);
    if (startTime.isBefore(DateTime.now()))
      startTime = startTime.plusDays(1);
    return startTime;
  }
}
