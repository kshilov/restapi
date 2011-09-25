package com.heymoose.job;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

  private final static Logger log = LoggerFactory.getLogger(Scheduler.class);

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final String targetHost;
  private final int runAtHours;
  private final int runAtMinutes;
  private final Runnable task;


  public Scheduler(String targetHost, int runAtHours, int runAtMinutes, Runnable task) {
    this.targetHost = targetHost;
    this.runAtHours = runAtHours;
    this.runAtMinutes = runAtMinutes;
    this.task = task;
  }

  private static class TaskWrapper implements Runnable {

    private final Runnable target;

    public TaskWrapper(Runnable target) {
      this.target = target;
    }

    @Override
    public void run() {
      log.info("Starting task");
      try {
        target.run();
        log.info("Task finished");
      } catch (Throwable e) {
        log.info("Error while executing task!", e);
      }
    }
  }

  public void schedule() {
    try {
      String localhostAddress = InetAddress.getLocalHost().getHostAddress();
      log.info("Localhost address: {}", localhostAddress);
      if (!localhostAddress.equals(targetHost)) {
        log.info("Target host ({}) is different, skipping", targetHost);
        return;
      }
    } catch (UnknownHostException e) {
      log.error("Error while get localhost", e);
    }

    DateTime startTime = StartTimeCalculator.calcStartTime(runAtHours, runAtMinutes);
    log.info("First execution time: {}", startTime);

    long delay = startTime.getMillis() - DateTime.now().getMillis();
    scheduler.scheduleAtFixedRate(new TaskWrapper(task), delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
  }
}
