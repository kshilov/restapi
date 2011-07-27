package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.rest.job.JobRunner;
import com.heymoose.rest.job.ReservationCleaner;
import org.hibernate.cfg.Configuration;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.heymoose.util.PropertiesUtil.subTree;

public class ProductionModule extends AbstractModule {

  private final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  
  @Override
  protected void configure() {
    bind(ReservationCleaner.class);
    bind(JobRunner.class).toProvider(jobRunnerProvider()).asEagerSingleton();
  }

  @Provides
  @Singleton
  protected Configuration hibernateConfig(@Named("entities") Set<Class> classes, @Named("settings") Properties settings) {
    Configuration config = new Configuration();

    for (Class klass : classes)
      config.addAnnotatedClass(klass);

    Properties hibernateProperties = subTree(settings, "hibernate", "hibernate");
    config.setProperties(hibernateProperties);
    
    return config;
  }

  protected Provider<JobRunner> jobRunnerProvider() {
    return new Provider<JobRunner>(){

      @Inject @Named("settings")
      private Properties settings;

      @Inject
      private ReservationCleaner cleaner;

      @Override
      public JobRunner get() {
        String targetHost = settings.getProperty("job-target-host").trim();
        JobRunner runner = new JobRunner(
                scheduler,
                targetHost,
                new Runnable() {
                  @Override
                  public void run() {
                    cleaner.deleteExpiredReservations();
                  }
                }
        );
        try {
          runner.startJob();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return runner;
      }
    };
  }
}