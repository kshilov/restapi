package com.heymoose.infrastructure.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.heymoose.infrastructure.job.Job;
import com.heymoose.infrastructure.job.Scheduler;
import com.heymoose.infrastructure.service.ActionImporter;
import com.heymoose.infrastructure.service.Mlm;
import com.heymoose.infrastructure.util.PropertiesUtil;
import org.hibernate.cfg.Configuration;
import org.joda.time.DateTime;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;
import static com.heymoose.infrastructure.util.PropertiesUtil.subTree;

public class ProductionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ActionImporter.class);
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

  @Provides
  @Singleton
  @Named("adv-map")
  protected Map<Long, URL> advMap(@Named("settings") Properties settings) throws MalformedURLException {
    Map<Long, URL> advMap = newHashMap();
    Properties advProps = PropertiesUtil.subTree(settings, "action-import", null);
    for (Map.Entry<Object, Object> ent : advProps.entrySet())
      advMap.put(Long.parseLong(ent.getKey().toString()), new URL(ent.getValue().toString()));
    return advMap;
  }

  @Provides
  @Singleton
  @Named("action-import-period")
  protected Integer actionImportPeriod(@Named("settings") Properties settings) {
    return Integer.valueOf(settings.get("action-import-period").toString());
  }

  @Provides
  @Singleton
  @Named("system")
  protected ScheduledExecutorService sched(@Named("settings") Properties settings, ActionImporter actionImporter) {
    long period = Long.valueOf(settings.get("action-import-period").toString());
    ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
    sched.scheduleAtFixedRate(actionImporter, 0, period, TimeUnit.HOURS);
    return sched;
  }

  @Provides @Singleton
  protected Scheduler scheduler(final Mlm mlm) {
    Scheduler scheduler = new Scheduler(3, 0, new Job() {
      @Override
      public void run(DateTime plannedStartTime) throws Exception {
        // Disabled temporarily
        //mlm.doExport();
      }
    });
    scheduler.schedule();
    return scheduler;
  }

  @Provides @Named("topshop.import.url") @Singleton
  protected String topshopUrl(@Named("settings") Properties settings) {
    return settings.get("topshop.import.url").toString();
  }

  @Provides @Named("topshop.offer") @Singleton
  protected String topshopOffer(@Named("settings") Properties settings) {
    return settings.get("topshop.offer").toString();
  }
}
