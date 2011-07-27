package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.hibernate.cfg.Configuration;

import java.util.Set;

public class ProductionModule extends AbstractModule {
  
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

    config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    config.setProperty("hibernate.show_sql", "true");
    config.setProperty("hibernate.format_sql", "false");
    config.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
    config.setProperty("hibernate.current_session_context_class", "thread");
    config.setProperty("hibernate.jdbc.batch_size", "0");
    return config;
  }
}