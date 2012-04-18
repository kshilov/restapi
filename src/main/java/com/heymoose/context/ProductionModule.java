package com.heymoose.context;

import static com.google.common.collect.Maps.newHashMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.heymoose.domain.affiliate.ActionImporter;
import com.heymoose.domain.affiliate.Tracking;
import com.heymoose.domain.affiliate.base.Repo;
import com.heymoose.util.PropertiesUtil;
import static com.heymoose.util.PropertiesUtil.subTree;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.hibernate.cfg.Configuration;

public class ProductionModule extends AbstractModule {

  @Override
  protected void configure() {
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
  protected ActionImporter actionImporter(@Named Properties settings, Tracking tracking, Repo repo) throws MalformedURLException {
    int period = Integer.valueOf(settings.get("action-import-period").toString());
    Map<Long, URL> advMap = newHashMap();
    Properties advProps = PropertiesUtil.subTree(settings, "action-import", null);
    for (Map.Entry<Object, Object> ent : advProps.entrySet())
      advMap.put(Long.parseLong(ent.getKey().toString()), new URL(ent.getValue().toString()));
    ActionImporter actionImporter = new ActionImporter(advMap, tracking, repo, period);
    return actionImporter;
  }

  @Provides
  @Singleton
  @Named("system")
  protected ScheduledExecutorService sched(@Named Properties settings, ActionImporter actionImporter) {
    long period = Long.valueOf(settings.get("action-import-period").toString());
    ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
    sched.scheduleAtFixedRate(actionImporter, 0, period, TimeUnit.HOURS);
    return sched;
  }
}
