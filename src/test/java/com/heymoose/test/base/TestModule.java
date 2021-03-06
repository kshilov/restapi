package com.heymoose.test.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.hibernate.cfg.Configuration;
import org.junit.Ignore;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Ignore
public class TestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PostbackTestResource.class);
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  protected Configuration hibernateConfig(@Named("entities") Set<Class> classes, Injector injector) {
    Configuration config = new Configuration();

    for (Class klass : classes)
      config.addAnnotatedClass(klass);

    /*
   config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
   config.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/hm_test");
   config.setProperty("hibernate.dialect", "com.heymoose.infrastructure.hibernate.PostgreSQLDialect");
   config.setProperty("hibernate.connection.username", "postgres");
   config.setProperty("hibernate.connection.password", "qwerty");
   config.setProperty("hibernate.hbm2ddl.auto", "none");  // "update"
    */

    config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
    config.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:heymoose");
    config.setProperty("hibernate.hbm2ddl.auto", "create");

    config.setProperty("hibernate.show_sql", "false");
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
    settings.setProperty("banners.dir", "target/banners");
    settings.setProperty("mlm-ratio", "0.1");
    settings.setProperty("tracker.host", "http://test.heymoose.com");
    return settings;
  }

  @Provides
  @Named("top-shop-offer-map")
  protected Map<String, Long> topShopOfferMap() {
    return ImmutableMap.of("top-shop-item-id", 1L);
  }

  @Provides
  protected InputSupplier<InputStream> topShopXmlSupplier() {
    URL topShopXml = getClass().getClassLoader()
        .getResource("topshop/example.xml");
    return Resources.newInputStreamSupplier(topShopXml);
  }

}
