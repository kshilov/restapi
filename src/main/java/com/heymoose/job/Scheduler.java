package com.heymoose.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scheduler {

  private final static Logger log = LoggerFactory.getLogger(Scheduler.class);

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final int runAtHours;
  private final int runAtMinutes;
  private final Job job;

  public Scheduler(int runAtHours, int runAtMinutes, Job job) {
    this.runAtHours = runAtHours;
    this.runAtMinutes = runAtMinutes;
    this.job = job;
  }

  private static class TaskWrapper implements Runnable {

    private final Job target;
    private volatile DateTime nextStartTime;

    public TaskWrapper(Job target, DateTime nextStartTime) {
      this.target = target;
      this.nextStartTime = nextStartTime;
    }

    @Override
    public void run() {
      log.info("Starting job");
      try {
        target.run(nextStartTime);
        nextStartTime = nextStartTime.plusDays(1);
        log.info("Task finished");
      } catch (Throwable e) {
        log.info("Error while executing job!", e);
      }
    }
  }

  public void schedule() {
    DateTime startTime = StartTimeCalculator.calcStartTime(runAtHours, runAtMinutes);
    log.info("First execution time: {}", startTime);

    long delay = startTime.getMillis() - DateTime.now().getMillis();
    scheduler.scheduleAtFixedRate(new TaskWrapper(job, startTime), delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
  }
}
