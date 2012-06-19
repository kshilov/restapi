package com.heymoose.context;

import com.heymoose.domain.affiliate.ActionImporter;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Named;

/**
 * @author Serg Prasolov ci.serg@gmail.com
 * @since 6/19/12 8:54 PM
 */
public class ConsoleModule extends ProductionModule {
  @Override
  protected ScheduledExecutorService sched(@Named("settings") Properties settings, ActionImporter actionImporter) {
    return Executors.newSingleThreadScheduledExecutor(); // empty
  }
}
