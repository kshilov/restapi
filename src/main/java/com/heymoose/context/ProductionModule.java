package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.heymoose.domain.Mlm;
import com.heymoose.events.EventBus;
import com.heymoose.job.Job;
import com.heymoose.job.Scheduler;
import com.heymoose.job.SettingsCalculatorTask;
import com.heymoose.rabbitmq.RabbitBus;
import com.heymoose.rabbitmq.RabbitMqSender;
import com.heymoose.util.PropertiesUtil;
import com.rabbitmq.client.ConnectionFactory;
import org.hibernate.cfg.Configuration;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.Set;

import static com.heymoose.util.PropertiesUtil.subTree;

public class ProductionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Mlm.class);
    bind(SettingsCalculatorTask.class);
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
      private SettingsCalculatorTask settingsCalculatorTask;

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
            settingsCalculatorTask.run(plannedStartTime);
          }
        });
        scheduler.schedule();
        return scheduler;
      }
    };
  }
}
