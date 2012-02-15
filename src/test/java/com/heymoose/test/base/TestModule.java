package com.heymoose.test.base;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.heymoose.events.Event;
import com.heymoose.events.EventBus;
import java.util.Properties;
import java.util.Set;
import org.hibernate.cfg.Configuration;
import org.junit.Ignore;

@Ignore
public class TestModule extends AbstractModule {
  
  @Override
  protected void configure() {
    bind(EventBus.class).toInstance(new EventBus() {
      @Override
      public void publish(Event event) {
        // do nothing
      }
    });
  }

  @Provides
  @Singleton
  protected Configuration hibernateConfig(@Named("entities") Set<Class> classes, Injector injector) {
    Configuration config = new Configuration();

    for (Class klass : classes)
      config.addAnnotatedClass(klass);

    config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    config.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:heymoose");
    config.setProperty("hibernate.hbm2ddl.auto", "create");
    config.setProperty("hibernate.show_sql", "true");
    config.setProperty("hibernate.format_sql", "false");
    config.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
    config.setProperty("hibernate.current_session_context_class", "thread");
    config.setProperty("hibernate.jdbc.batch_size", "0");
    return config;
  }

  @Provides
  @Singleton
  @Named("settings")
  protected Properties settings() {
    Properties settings = new Properties();
    settings.setProperty("tax", "0.1");
    settings.setProperty("robokassaPass", "robokassaPass");
    return settings;
  }
}
