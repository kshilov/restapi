package com.heymoose.context;

import com.google.inject.AbstractModule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ProductionModule extends AbstractModule {

  private final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  
  @Override
  protected void configure() {
    //bind(ReservationCleaner.class);
    //bind(JobRunner.class).toProvider(jobRunnerProvider()).asEagerSingleton();
  }

 /* @Provides
  @Singleton
  protected Configuration hibernateConfig(@Named("entities") Set<Class> classes, @Named("settings") Properties settings) {
    Configuration config = new Configuration();

    for (Class klass : classes)
      config.addAnnotatedClass(klass);

    Properties hibernateProperties = subTree(settings, "hibernate", "hibernate");
    config.setProperties(hibernateProperties);
    
    return config;
  }*/

  /*
  protected Provider<JobRunner> jobRunnerProvider() {
    return new Provider<JobRunner>(){

      @Inject @Named("settings")
      private Properties settings;

      @Inject
      private ReservationCleaner cleaner;

      @Override
      public JobRunner byId() {
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
  }*/
}