package com.heymoose.rest.context;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.heymoose.hibernate.Transactional;
import com.heymoose.hibernate.TxInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bindInterceptor(
				Matchers.any(),
				Matchers.annotatedWith(Transactional.class),
				new TxInterceptor(getProvider(Session.class)));
    bind(SessionFactory.class).toProvider(sessionFactoryProvider()).asEagerSingleton();
	}

  protected Provider<SessionFactory> sessionFactoryProvider() {
    return new Provider<SessionFactory>(){

      private @Inject Configuration config;

      @Override
      public SessionFactory get() {
        return config.buildSessionFactory();
      }
    };
  }

	@Provides
	protected Session session(SessionFactory sessionFactory) {
		return sessionFactory.getCurrentSession();
	}
}
