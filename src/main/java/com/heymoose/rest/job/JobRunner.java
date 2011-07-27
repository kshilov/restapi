package com.heymoose.rest.job;

import java.net.InetAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobRunner {

  private final ScheduledExecutorService scheduler;
  private final String targetHost;
  private final Runnable task;

  public void startJob() throws Exception {
    String localHost = InetAddress.getLocalHost().getHostAddress();
    if (!localHost.equals(targetHost))
      return;
     scheduler.scheduleWithFixedDelay(task, 0, 1, TimeUnit.MINUTES);
  }

  public JobRunner(ScheduledExecutorService scheduler, String targetHost, Runnable task) {
    this.scheduler = scheduler;
    this.targetHost = targetHost;
    this.task = task;
  }
}
