package com.heymoose.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import static com.heymoose.util.PropertiesUtil.subTree;
import java.util.Properties;
import java.util.Set;
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
}
