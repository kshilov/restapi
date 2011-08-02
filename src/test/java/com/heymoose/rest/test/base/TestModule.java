package com.heymoose.rest.test.base;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.hibernate.cfg.Configuration;
import org.junit.Ignore;

import java.util.Properties;
import java.util.Set;

@Ignore
public class TestModule extends AbstractModule {
  @Override
  protected void configure() {
    
  }

  @Provides
  @Singleton
  protected Configuration hibernateConfig(@Named("entities") Set<Class> classes) {
    Configuration config = new Configuration();

    for (Class klass : classes)
      config.addAnnotatedClass(klass);

    config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    config.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:heymoose");
    config.setProperty("hibernate.connection.username", "sa");
    config.setProperty("hibernate.connection.password", "");
    config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

    /*config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
    config.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/heymoose");
    config.setProperty("hibernate.connection.username", "postgres");
    config.setProperty("hibernate.connection.password", "");
    config.setProperty("hibernate.dialect", "com.heymoose.hibernate.PostgreSQLDialect");*/

    config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
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
    settings.setProperty("answer-cost", "15.0");
    settings.setProperty("max-shows", "1000");
    return settings;
  }
}
