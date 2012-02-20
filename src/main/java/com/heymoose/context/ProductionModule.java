package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.heymoose.domain.Mlm;
import com.heymoose.job.AppStatCalculatorTask;
import com.heymoose.job.Job;
import com.heymoose.job.Scheduler;
import com.heymoose.job.UserStatCalculatorTask;
import com.heymoose.util.PropertiesUtil;
import static com.heymoose.util.PropertiesUtil.subTree;
import java.util.Properties;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.hibernate.cfg.Configuration;
import org.joda.time.DateTime;

public class ProductionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Mlm.class);
    bind(UserStatCalculatorTask.class);
    bind(AppStatCalculatorTask.class);
    bind(Scheduler.class).toProvider(schedulerProvider()).asEagerSingleton();
  }

  @Provides
  @Singleton
  protected Configuration hibernateConfig(@Named("entities") Set<Class> classes, @Named("settings") Properties settings) {
    Configuration config = new Configuration();

    for (Class klass : classes)
      config.addAnnotatedClass(klass);

    Properties hibernateProperties = subTree(settings, "hibernate", "hibernate");
    config.setProperties(hibernateProperties);

    Properties bonecpProperties = subTree(settings, "bonecp", "bonecp");
    config.addProperties(bonecpProperties);

    return config;
  }


  protected Provider<Scheduler> schedulerProvider() {
    return new Provider<Scheduler>() {

      @Inject @Named("settings")
      private Properties settings;

      @Inject
      private Mlm mlm;
      
      @Inject
      private UserStatCalculatorTask userStatCalculatorTask;
      
      @Inject
      private AppStatCalculatorTask appStatCalculatorTask;

      @Override
      public Scheduler get() {
        Properties schedProps = PropertiesUtil.subTree(settings, "scheduler", null);
        String targetHost = schedProps.getProperty("target-host").trim();
        int runAtHours = Integer.parseInt(schedProps.getProperty("run-at-hours"));
        int runAtMinutes = Integer.parseInt(schedProps.getProperty("run-at-minutes"));
        Scheduler scheduler = new Scheduler(targetHost, runAtHours, runAtMinutes, new Job() {
          @Override
          public void run(DateTime plannedStartTime) throws Exception {
            mlm.doMlmExport(plannedStartTime);
            userStatCalculatorTask.run(plannedStartTime);
            appStatCalculatorTask.run(plannedStartTime, false);
          }
        });
        scheduler.schedule();
        return scheduler;
      }
    };
  }
}
