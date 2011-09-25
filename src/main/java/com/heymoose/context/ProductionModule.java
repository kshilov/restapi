package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.heymoose.job.Scheduler;
import com.heymoose.util.PropertiesUtil;
import org.hibernate.cfg.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.Set;

import static com.heymoose.util.PropertiesUtil.subTree;

public class ProductionModule extends AbstractModule {

  @Override
  protected void configure() {
//    bind(Scheduler.class).toProvider(schedulerProvider()).asEagerSingleton();
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

  protected Provider<Scheduler> schedulerProvider() {
    return new Provider<Scheduler>() {

      @Inject @Named("settings")
      private Properties settings;

      @Override
      public Scheduler get() {
        Properties schedProps = PropertiesUtil.subTree(settings, "scheduler", "");
        String targetHost = schedProps.getProperty("target-host").trim();
        int runAtHours = Integer.parseInt(schedProps.getProperty("ru-at-hours"));
        int runAtMinutes = Integer.parseInt(schedProps.getProperty("ru-at-minutes"));
        Scheduler scheduler = new Scheduler(targetHost, runAtHours, runAtMinutes, null);
        scheduler.schedule();
        return scheduler;
      }
    };
  }
}
